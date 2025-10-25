package com.androidinsta.Service

import com.androidinsta.config.KafkaConfig
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumerService(
    private val redisService: RedisService
) {

    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)

    @KafkaListener(topics = [KafkaConfig.POST_CREATED_TOPIC], groupId = "androidinsta-group")
    fun consumePostCreated(message: Map<String, Any>) {
        try {
            logger.info("Consumed POST_CREATED event: $message")
            
            // Process post created event
            // - Update analytics
            // - Send notifications to followers
            // - Update user activity cache
            val postId = (message["postId"] as Number).toLong()
            val userId = (message["userId"] as Number).toLong()
            
            // Example: Invalidate user cache to refresh post count
            redisService.invalidateUserCache(userId)
            
            logger.info("Processed post created: postId=$postId, userId=$userId")
        } catch (e: Exception) {
            logger.error("Error processing POST_CREATED event", e)
        }
    }

    @KafkaListener(topics = [KafkaConfig.POST_LIKED_TOPIC], groupId = "androidinsta-group")
    fun consumePostLiked(message: Map<String, Any>) {
        try {
            logger.info("Consumed POST_LIKED event: $message")
            
            val postId = (message["postId"] as Number).toLong()
            val userId = (message["userId"] as Number).toLong()
            
            // Process like event
            // - Send notification to post owner
            // - Update like count cache
            // - Update trending posts
            
            logger.info("Processed post liked: postId=$postId, userId=$userId")
        } catch (e: Exception) {
            logger.error("Error processing POST_LIKED event", e)
        }
    }

    @KafkaListener(topics = [KafkaConfig.POST_COMMENTED_TOPIC], groupId = "androidinsta-group")
    fun consumePostCommented(message: Map<String, Any>) {
        try {
            logger.info("Consumed POST_COMMENTED event: $message")
            
            val postId = (message["postId"] as Number).toLong()
            val commentId = (message["commentId"] as Number).toLong()
            val userId = (message["userId"] as Number).toLong()
            
            // Process comment event
            // - Send notification to post owner and mentioned users
            // - Update comment count cache
            
            logger.info("Processed post commented: postId=$postId, commentId=$commentId, userId=$userId")
        } catch (e: Exception) {
            logger.error("Error processing POST_COMMENTED event", e)
        }
    }

    @KafkaListener(topics = [KafkaConfig.USER_REGISTERED_TOPIC], groupId = "androidinsta-group")
    fun consumeUserRegistered(message: Map<String, Any>) {
        try {
            logger.info("Consumed USER_REGISTERED event: $message")
            
            val userId = (message["userId"] as Number).toLong()
            val username = message["username"] as String
            val email = message["email"] as String
            
            // Process user registration
            // - Send welcome email
            // - Create default user preferences
            // - Initialize user statistics
            
            logger.info("Processed user registered: userId=$userId, username=$username")
        } catch (e: Exception) {
            logger.error("Error processing USER_REGISTERED event", e)
        }
    }

    @KafkaListener(topics = [KafkaConfig.USER_FOLLOWED_TOPIC], groupId = "androidinsta-group")
    fun consumeUserFollowed(message: Map<String, Any>) {
        try {
            logger.info("Consumed USER_FOLLOWED event: $message")
            
            val followerId = (message["followerId"] as Number).toLong()
            val followedId = (message["followedId"] as Number).toLong()
            
            // Process follow event
            // - Send notification to followed user
            // - Update follower/following counts cache
            // - Update feed recommendations
            
            redisService.invalidateUserCache(followerId)
            redisService.invalidateUserCache(followedId)
            
            logger.info("Processed user followed: followerId=$followerId, followedId=$followedId")
        } catch (e: Exception) {
            logger.error("Error processing USER_FOLLOWED event", e)
        }
    }

    @KafkaListener(topics = [KafkaConfig.NOTIFICATION_TOPIC], groupId = "androidinsta-group")
    fun consumeNotification(message: Map<String, Any>) {
        try {
            logger.info("Consumed NOTIFICATION event: $message")
            
            val userId = (message["userId"] as Number).toLong()
            val title = message["title"] as String
            val notificationMessage = message["message"] as String
            val type = message["type"] as String
            
            // Process notification
            // - Send push notification
            // - Send email if configured
            // - Store in notification table
            
            logger.info("Processed notification: userId=$userId, type=$type, title=$title")
        } catch (e: Exception) {
            logger.error("Error processing NOTIFICATION event", e)
        }
    }
}
