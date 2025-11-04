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
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
        
        // Lấy danh sách IDs của người mình follow
        val followedUserIds = user.following.map { it.followed.id }.toMutableList()
        
        // Thêm chính user hiện tại để xem posts của mình
        followedUserIds.add(userId)
        
        // Lấy posts: từ người follow HOẶC posts public
        return postRepository.findFeedPosts(followedUserIds, pageable)
    }
    
    /**
     * Lấy posts của một user cụ thể
     */
    fun getUserPosts(userId: Long, currentUserId: Long?, pageable: Pageable): Page<Post> {
        // Nếu xem posts của người khác, chỉ hiển thị PUBLIC
        // Nếu xem posts của chính mình, hiển thị tất cả
        return if (currentUserId != null && currentUserId == userId) {
            postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        } else {
            postRepository.findByUserIdAndVisibilityOrderByCreatedAtDesc(
                userId, 
                Visibility.PUBLIC, 
                pageable
            )
        }
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
        
        // Gửi notification cho tất cả bạn bè
        val friendIds = friendService.getFriendIds(userId)
        friendIds.forEach { friendId ->
            notificationService.sendNotification(
                receiverId = friendId,
                senderId = userId,
                type = com.androidinsta.Model.NotificationType.POST,
                entityId = savedPost.id,
                message = "${user.username} đã đăng bài viết mới"
            )
        }
        
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
    }
    
    /**
     * Lấy ADVERTISE posts (quảng cáo)
     */
    fun getAdvertisePosts(pageable: Pageable): Page<Post> {
        return postRepository.findAdvertisePosts(pageable)
    }
}
