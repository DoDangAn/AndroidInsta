package com.androidinsta.Service

import com.androidinsta.Model.Post
import com.androidinsta.Model.Visibility
import com.androidinsta.Model.MediaFile
import com.androidinsta.Model.MediaType
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.dto.*
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
    private val followService: FollowService
) {
    
    /**
     * Lấy feed response DTO cho user
     * DON'T cache complex DTOs - query is fast with database indexes
     * Professional approach: cache simple counters only, build DTOs fresh
     */
    fun getFeedResponse(userId: Long, pageable: Pageable): com.androidinsta.dto.FeedResponse {
        val posts = getFeedPosts(userId, pageable)
        return com.androidinsta.dto.FeedResponse(
            posts = posts.content.map { it.toDto(userId) },
            currentPage = posts.number,
            totalPages = posts.totalPages,
            totalItems = posts.totalElements
        )
    }
    
    /**
     * Lấy feed posts cho user: CHỈ bao gồm posts của người mình follow + posts của chính mình + ADVERTISE
     * KHÔNG hiển thị random public posts (giống Instagram)
     * Internal method không cache - chỉ dùng bởi getFeedResponse
     */
    private fun getFeedPosts(userId: Long, pageable: Pageable): Page<Post> {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
        
        // Lấy danh sách IDs của người mình follow
        val followedUserIds = user.following.map { it.followed.id }.toMutableList()
        
        // Thêm chính user hiện tại để xem posts của mình
        followedUserIds.add(userId)
        
        // Lấy posts: CHỈ từ người follow + chính mình + ADVERTISE posts
        // KHÔNG bao gồm random public posts từ người lạ
        return postRepository.findFeedPosts(followedUserIds, pageable)
    }
    
    /**
     * Lấy user posts response DTO
     * DON'T cache complex DTOs - query is fast with database indexes
     */
    fun getUserPostsResponse(userId: Long, currentUserId: Long?, pageable: Pageable): com.androidinsta.dto.FeedResponse {
        val posts = getUserPosts(userId, currentUserId, pageable)
        return com.androidinsta.dto.FeedResponse(
            posts = posts.content.map { it.toDto(currentUserId) },
            currentPage = posts.number,
            totalPages = posts.totalPages,
            totalItems = posts.totalElements
        )
    }
    
    /**
     * Lấy posts của một user cụ thể
     * Internal method không cache
     */
    private fun getUserPosts(userId: Long, currentUserId: Long?, pageable: Pageable): Page<Post> {
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
     * Tạo post mới với media files
     * 
     * Business Logic:
     * - Validate user tồn tại trong hệ thống
     * - Tạo MediaFile objects từ URLs (hỗ trợ image và video)
     * - Save post vào database với cascade persist cho media files
     * - Gửi Kafka event để audit log và real-time notifications
     * - Invalidate user cache để đảm bảo data consistency
     * 
     * Transaction: REQUIRED - Rollback nếu có lỗi
     * 
     * @param userId ID của user tầo post (must exist)
     * @param caption Nội dung caption (optional, max TEXT length)
     * @param visibility Mức độ hiển thị: PUBLIC, PRIVATE, ADVERTISE
     * @param mediaUrls Danh sách URLs của media files (Cloudinary URLs)
     * @return Post entity vừa được tạo với ID generated
     * @throws RuntimeException nếu user không tồn tại
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
        
        // Send notification to all followers about new post
        // Only send for PUBLIC posts (not PRIVATE or ADVERTISE)
        if (visibility == Visibility.PUBLIC) {
            val followers = followService.getFollowers(userId)
            followers.forEach { follower ->
                try {
                    notificationService.sendNotification(
                        receiverId = follower.id,
                        senderId = userId,
                        type = com.androidinsta.Model.NotificationType.NEW_POST,
                        entityId = savedPost.id,
                        message = null
                    )
                } catch (e: Exception) {
                    println("Error sending notification to follower ${follower.id}: ${e.message}")
                }
            }
        }
        
        // Send Kafka event for post creation
        kafkaProducerService.sendPostCreatedEvent(
            postId = savedPost.id,
            userId = userId,
            content = caption
        )
        
        // Invalidate simple caches (counters only, NOT DTOs)
        redisService.delete("user:${userId}:postCount")
        redisService.delete("user:${userId}:stats")
        
        return savedPost
    }
    
    /**
     * Xóa post và tất cả related data (comments, likes, saved posts)
     * 
     * Security:
     * - Kiểm tra ownership: Chỉ chủ post mới được xóa
     * - Cascade delete: Tự động xóa comments, likes, saved_posts vì orphanRemoval=true
     * 
     * Cache Strategy:
     * - Invalidate user cache của chủ post
     * - Invalidate tất cả feed cache (vì post đã biến mất khỏi feed)
     * - Invalidate user posts cache
     * 
     * Audit:
     * - Gửi Kafka event để log hành động xóa
     * 
     * @param postId ID của post cần xóa
     * @param userId ID của user thực hiện xóa (must be post owner)
     * @throws RuntimeException nếu post không tồn tại hoặc user không phải chủ
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
        
        // Invalidate simple caches (counters only, NOT DTOs)
        redisService.delete("user:${userId}:postCount")
        redisService.delete("user:${userId}:stats")
        redisService.delete("post:${postId}:likeCount")
        redisService.delete("post:${postId}:commentCount")
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
        
        // No cache invalidation needed for caption/visibility updates
        // (Counters don't change, DTOs not cached)
        redisService.delete("feed:posts:*")
        redisService.delete("user:posts:$userId:*")
        
        return updated
    }
    
    /**
     * Lấy PUBLIC posts từ 7 ngày gần đây
     * Dùng cho Explore/Recent Posts feature
     */
    fun getAdvertisePosts(pageable: Pageable): Page<Post> {
        val oneWeekAgo = java.time.LocalDateTime.now().minusWeeks(1)
        return postRepository.findRecentPublicPosts(oneWeekAgo, pageable)
    }
    
    /**
     * Lấy một post theo ID với permission check
     * 
     * Permission Logic:
     * - PUBLIC/ADVERTISE: Ai cũng xem được (kể cả guest users)
     * - PRIVATE: Chỉ chủ post xem được
     * 
     * Cache Strategy:
     * - Cache key: "post:detail:postId:userId" (10 minutes TTL)
     * - Separate cache cho mỗi user vì permission khác nhau
     * - Guest users: userId = "guest"
     * 
     * @param postId ID của post cần lấy
     * @param currentUserId ID của user hiện tại (nullable cho guest users)
     * @return Post entity với lazy-loaded relationships
     * @throws RuntimeException nếu post không tồn tại hoặc không có permission
     */
    fun getPostById(postId: Long, currentUserId: Long?): Post {
        // DON'T cache Post entity - too complex, query is fast with DB indexes
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
        }
        
        // DON'T cache Post entity - too complex, query is fast with DB indexes
        return post
    }
}
