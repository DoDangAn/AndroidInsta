package com.androidinsta.Service

import com.androidinsta.Model.Like
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Repository
interface LikeRepository : JpaRepository<Like, Long> {
    fun findByPostIdAndUserId(postId: Long, userId: Long): Like?
    fun existsByPostIdAndUserId(postId: Long, userId: Long): Boolean
    fun deleteByPostIdAndUserId(postId: Long, userId: Long)
    fun countByPostId(postId: Long): Long
}

@Service
@Transactional
class LikeService(
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val redisService: RedisService
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

        // Send Kafka event
        kafkaProducerService.sendPostLikedEvent(postId, userId)

        // Send notification to post owner if different user
        if (post.user.id != userId) {
            kafkaProducerService.sendNotificationEvent(
                userId = post.user.id,
                title = "New Like",
                message = "${user.username} liked your post",
                type = "LIKE"
            )
        }

        return true
    }

    fun unlikePost(userId: Long, postId: Long): Boolean {
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return false // Not liked
        }

        likeRepository.deleteByPostIdAndUserId(postId, userId)
        return true
    }

    fun isPostLikedByUser(postId: Long, userId: Long): Boolean {
        return likeRepository.existsByPostIdAndUserId(postId, userId)
    }

    fun getLikeCount(postId: Long): Long {
        return likeRepository.countByPostId(postId)
    }
}
