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
     * Send user unfollowed event
     */
    fun sendUserUnfollowedEvent(followerId: Long, followedId: Long) {
        val event = mapOf(
            "followerId" to followerId,
            "followedId" to followedId,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.USER_UNFOLLOWED_TOPIC, followerId.toString(), event)
    }

    /**
     * Send post unliked event
     */
    fun sendPostUnlikedEvent(postId: Long, userId: Long) {
        val event = mapOf(
            "postId" to postId,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.POST_UNLIKED_TOPIC, postId.toString(), event)
    }

    /**
     * Send post deleted event
     */
    fun sendPostDeletedEvent(postId: Long, userId: Long) {
        val event = mapOf(
            "postId" to postId,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.POST_DELETED_TOPIC, postId.toString(), event)
    }

    /**
     * Send post updated event
     */
    fun sendPostUpdatedEvent(postId: Long, userId: Long) {
        val event = mapOf(
            "postId" to postId,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.POST_UPDATED_TOPIC, postId.toString(), event)
    }

    /**
     * Send message sent event
     */
    fun sendMessageSentEvent(messageId: Long, senderId: Long, receiverId: Long, content: String) {
        val event = mapOf(
            "messageId" to messageId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.MESSAGE_SENT_TOPIC, messageId.toString(), event)
    }

    /**
     * Send message deleted event
     */
    fun sendMessageDeletedEvent(messageId: Long, userId: Long) {
        val event = mapOf(
            "messageId" to messageId,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.MESSAGE_DELETED_TOPIC, messageId.toString(), event)
    }

    /**
     * Send friend request accepted event
     */
    fun sendFriendAcceptEvent(userId: Long, friendId: Long) {
        val event = mapOf(
            "userId" to userId,
            "friendId" to friendId,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.FRIEND_ACCEPTED_TOPIC, userId.toString(), event)
    }

    /**
     * Send password changed event
     */
    fun sendPasswordChangedEvent(userId: Long, username: String) {
        val event = mapOf(
            "userId" to userId,
            "username" to username,
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.PASSWORD_CHANGED_TOPIC, userId.toString(), event)
    }

    /**
     * Send admin post deleted event
     */
    fun sendAdminPostDeletedEvent(postId: Long, userId: Long) {
        val event = mapOf(
            "postId" to postId,
            "userId" to userId,
            "deletedBy" to "ADMIN",
            "timestamp" to System.currentTimeMillis()
        )
        send(KafkaConfig.ADMIN_POST_DELETED_TOPIC, postId.toString(), event)
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
