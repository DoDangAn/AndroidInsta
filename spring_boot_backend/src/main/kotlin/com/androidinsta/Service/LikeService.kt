package com.androidinsta.Service

import com.androidinsta.Model.Like
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Repository.User.LikeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class LikeService(
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val redisService: RedisService,
    private val notificationService: NotificationService,
    private val friendshipRepository: com.androidinsta.Repository.User.FriendshipRepository
) {

    fun likePost(userId: Long, postId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found") }

        // Check if already liked
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return false // Already liked
        }

        val like = Like(
            user = user,
            post = post
        )

        likeRepository.save(like)

        // Invalidate simple counter cache
        redisService.delete("post:${postId}:likeCount")

        // Send Kafka event
        kafkaProducerService.sendPostLikedEvent(postId, userId)

        // Gửi notification cho chủ bài viết (chỉ khi là bạn bè)
        if (post.user.id != userId && friendshipRepository.areFriends(userId, post.user.id)) {
            notificationService.sendNotification(
                receiverId = post.user.id,
                senderId = userId,
                type = com.androidinsta.Model.NotificationType.LIKE,
                entityId = postId,
                message = "${user.username} đã thích bài viết của bạn"
            )
        }

        return true
    }

    fun unlikePost(userId: Long, postId: Long): Boolean {
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return false // Not liked
        }

        likeRepository.deleteByPostIdAndUserId(postId, userId)
        
        // Invalidate simple counter cache
        redisService.delete("post:${postId}:likeCount")
        
        // Send Kafka event for audit
        kafkaProducerService.sendPostUnlikedEvent(postId, userId)
        
        return true
    }

    fun isPostLikedByUser(postId: Long, userId: Long): Boolean {
        return likeRepository.existsByPostIdAndUserId(postId, userId)
    }

    fun getLikeCount(postId: Long): Long {
        // ✅ Cache simple counter (Long) - this is good!
        val cacheKey = "post:${postId}:likeCount"
        
        // Use generic get() and handle both Integer and Long from Redis
        val cached = redisService.get(cacheKey)
        if (cached != null) {
            return when (cached) {
                is Long -> cached
                is Int -> cached.toLong()
                is Number -> cached.toLong()
                else -> {
                    // Invalid type, recompute
                    val count = likeRepository.countByPostId(postId)
                    redisService.set(cacheKey, count, java.time.Duration.ofMinutes(10))
                    count
                }
            }
        }
        
        val count = likeRepository.countByPostId(postId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(10))
        return count
    }
}
