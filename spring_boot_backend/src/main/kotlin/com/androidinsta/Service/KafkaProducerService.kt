package com.androidinsta.Service

import com.androidinsta.config.KafkaConfig
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)

    /**
     * Send post created event
     */
    fun sendPostCreatedEvent(postId: Long, userId: Long, content: String) {
        val event = mapOf(
            "postId" to postId,
            "userId" to userId,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.POST_CREATED_TOPIC, postId.toString(), event)
    }

    /**
     * Send post liked event
     */
    fun sendPostLikedEvent(postId: Long, userId: Long) {
        val event = mapOf(
            "postId" to postId,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.POST_LIKED_TOPIC, postId.toString(), event)
    }

    /**
     * Send post commented event
     */
    fun sendPostCommentedEvent(postId: Long, commentId: Long, userId: Long, content: String) {
        val event = mapOf(
            "postId" to postId,
            "commentId" to commentId,
            "userId" to userId,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.POST_COMMENTED_TOPIC, postId.toString(), event)
    }

    /**
     * Send user registered event
     */
    fun sendUserRegisteredEvent(userId: Long, username: String, email: String) {
        val event = mapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.USER_REGISTERED_TOPIC, userId.toString(), event)
    }

    /**
     * Send user followed event
     */
    fun sendUserFollowedEvent(followerId: Long, followedId: Long) {
        val event = mapOf(
            "followerId" to followerId,
            "followedId" to followedId,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.USER_FOLLOWED_TOPIC, followerId.toString(), event)
    }

    /**
     * Send notification event
     */
    fun sendNotificationEvent(userId: Long, title: String, message: String, type: String) {
        val event = mapOf(
            "userId" to userId,
            "title" to title,
            "message" to message,
            "type" to type,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.NOTIFICATION_TOPIC, userId.toString(), event)
    }

    /**
     * Generic send method
     */
    private fun send(topic: String, key: String, message: Any) {
        try {
            kafkaTemplate.send(topic, key, message).whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("Message sent to topic: $topic, key: $key, offset: ${result?.recordMetadata?.offset()}")
                } else {
                    logger.error("Failed to send message to topic: $topic, key: $key", ex)
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending message to Kafka topic: $topic", e)
        }
    }
}
