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

        // Invalidate cache
        redisService.delete("like:count:$postId")

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
        
        // Invalidate cache
        redisService.delete("like:count:$postId")
        
        // Send Kafka event for audit
        kafkaProducerService.sendPostUnlikedEvent(postId, userId)
        
        return true
    }

    fun isPostLikedByUser(postId: Long, userId: Long): Boolean {
        return likeRepository.existsByPostIdAndUserId(postId, userId)
    }

    fun getLikeCount(postId: Long): Long {
        val cacheKey = "like:count:$postId"
        val cached = redisService.get(cacheKey, Long::class.java)
        if (cached != null) return cached
        
        val count = likeRepository.countByPostId(postId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(10))
        return count
    }
}
