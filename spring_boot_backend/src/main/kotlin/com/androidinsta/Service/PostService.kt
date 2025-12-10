package com.androidinsta.Service

import com.androidinsta.Model.Post
import com.androidinsta.Model.Visibility
import com.androidinsta.Model.MediaFile
import com.androidinsta.Model.MediaType
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val redisService: RedisService,
    private val notificationService: NotificationService,
    private val friendService: FriendService
) {
    
    /**
     * Lấy feed posts cho user: bao gồm posts của người mình follow + posts public
     */
    fun getFeedPosts(userId: Long, pageable: Pageable): Page<Post> {
        val cacheKey = "feed:posts:$userId:${pageable.pageNumber}:${pageable.pageSize}"
        val cached = redisService.get(cacheKey)
        if (cached != null && cached is Page<*>) {
            @Suppress("UNCHECKED_CAST")
            return cached as Page<Post>
        }
        
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
        
        // Lấy danh sách IDs của người mình follow
        val followedUserIds = user.following.map { it.followed.id }.toMutableList()
        
        // Thêm chính user hiện tại để xem posts của mình
        followedUserIds.add(userId)
        
        // Lấy posts: từ người follow HOẶC posts public
        val result = postRepository.findFeedPosts(followedUserIds, pageable)
        redisService.set(cacheKey, result, java.time.Duration.ofMinutes(5))
        return result
    }
    
    /**
     * Lấy posts của một user cụ thể
     */
    fun getUserPosts(userId: Long, currentUserId: Long?, pageable: Pageable): Page<Post> {
        val cacheKey = "user:posts:$userId:${currentUserId ?: "guest"}:${pageable.pageNumber}:${pageable.pageSize}"
        val cached = redisService.get(cacheKey)
        if (cached != null && cached is Page<*>) {
            @Suppress("UNCHECKED_CAST")
            return cached as Page<Post>
        }
        
        // Nếu xem posts của người khác, chỉ hiển thị PUBLIC
        // Nếu xem posts của chính mình, hiển thị tất cả
        val result = if (currentUserId != null && currentUserId == userId) {
            postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        } else {
            postRepository.findByUserIdAndVisibilityOrderByCreatedAtDesc(
                userId, 
                Visibility.PUBLIC, 
                pageable
            )
        }
        
        redisService.set(cacheKey, result, java.time.Duration.ofMinutes(10))
        return result
    }
    
    /**
     * Tạo post mới
     */
    @Transactional
    fun createPost(userId: Long, caption: String, visibility: Visibility, mediaUrls: List<String>): Post {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
        
        // Tạo media files list trước
        val mediaFilesList = mediaUrls.mapIndexed { index, url ->
            MediaFile(
                fileUrl = url,
                fileType = if (url.contains(".mp4")) MediaType.VIDEO else MediaType.IMAGE,
                orderIndex = index
            )
        }
        
        val post = Post(
            user = user,
            caption = caption,
            visibility = visibility,
            mediaFiles = mediaFilesList
        )
        
        val savedPost = postRepository.save(post)
        
        // Send Kafka event for post creation
        kafkaProducerService.sendPostCreatedEvent(
            postId = savedPost.id,
            userId = userId,
            content = caption
        )
        
        // Invalidate user cache
        redisService.invalidateUserCache(userId)
        
        return savedPost
    }
    
    /**
     * Xóa post
     */
    @Transactional
    fun deletePost(postId: Long, userId: Long) {
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found") }
        
        if (post.user.id != userId) {
            throw RuntimeException("You can only delete your own posts")
        }
        
        postRepository.delete(post)
        
        // Send Kafka event for audit
        kafkaProducerService.sendPostDeletedEvent(postId, userId)
        
        // Invalidate caches
        redisService.invalidateUserCache(userId)
        redisService.delete("feed:posts:*")
        redisService.delete("user:posts:$userId:*")
    }

    /**
     * Cập nhật post
     */
    @Transactional
    fun updatePost(postId: Long, userId: Long, caption: String?, visibility: Visibility?): Post {
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found") }
            
        if (post.user.id != userId) {
            throw RuntimeException("You can only update your own posts")
        }
        
        if (caption != null) {
            post.caption = caption
        }
        
        if (visibility != null) {
            post.visibility = visibility
        }
        
        val updated = postRepository.save(post)
        
        // Send Kafka event for audit
        kafkaProducerService.sendPostUpdatedEvent(postId, userId)
        
        // Invalidate caches
        redisService.invalidateUserCache(userId)
        redisService.delete("feed:posts:*")
        redisService.delete("user:posts:$userId:*")
        
        return updated
    }
    
    /**
     * Lấy PUBLIC posts (bài viết công khai gần đây)
     */
    fun getAdvertisePosts(pageable: Pageable): Page<Post> {
        return postRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC, pageable)
    }
    
    /**
     * Lấy một post theo ID với permission check
     */
    fun getPostById(postId: Long, currentUserId: Long?): Post {
        val cacheKey = "post:detail:$postId:${currentUserId ?: "guest"}"
        val cached = redisService.get(cacheKey)
        if (cached != null && cached is Post) {
            return cached
        }
        
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found") }
        
        // Permission check
        when (post.visibility) {
            Visibility.PUBLIC, Visibility.ADVERTISE -> {
                // Ai cũng xem được
            }
            Visibility.PRIVATE -> {
                // Chỉ chủ post xem được
                if (currentUserId == null || currentUserId != post.user.id) {
                    throw RuntimeException("You don't have permission to view this post")
                }
            }
            Visibility.FRIENDS_ONLY -> {
                // Chỉ bạn bè mới xem được
                if (currentUserId == null) {
                    throw RuntimeException("You must be logged in to view this post")
                }
                if (currentUserId != post.user.id && !friendService.areFriends(currentUserId, post.user.id)) {
                    throw RuntimeException("Only friends can view this post")
                }
            }
        }
        
        redisService.set(cacheKey, post, java.time.Duration.ofMinutes(10))
        return post
    }
}
