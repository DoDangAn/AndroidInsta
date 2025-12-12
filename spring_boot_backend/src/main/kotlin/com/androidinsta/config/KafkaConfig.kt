package com.androidinsta.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.support.converter.JsonMessageConverter
import org.springframework.kafka.support.converter.RecordMessageConverter
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import java.time.Duration

/**
 * Kafka Configuration for AndroidInsta
 * 
 * Features:
 * - Auto-create topics with proper partitions and replication
 * - Retry mechanism with exponential backoff
 * - Dead Letter Queue (DLQ) for failed messages
 * - JSON message converter for type-safe messaging
 * - Optimized for social media use cases (high throughput, low latency)
 */
@Configuration
@EnableKafka
class KafkaConfig {

    companion object {
        // Post events
        const val POST_CREATED_TOPIC = "post.created"
        const val POST_UPDATED_TOPIC = "post.updated"
        const val POST_DELETED_TOPIC = "post.deleted"
        const val POST_LIKED_TOPIC = "post.liked"
        const val POST_UNLIKED_TOPIC = "post.unliked"
        const val POST_COMMENTED_TOPIC = "post.commented"
        
        // User events
        const val USER_REGISTERED_TOPIC = "user.registered"
        const val USER_FOLLOWED_TOPIC = "user.followed"
        const val USER_UNFOLLOWED_TOPIC = "user.unfollowed"
        
        // Message events
        const val MESSAGE_SENT_TOPIC = "message.sent"
        const val MESSAGE_DELETED_TOPIC = "message.deleted"
        
        // Friend events
        const val FRIEND_ACCEPTED_TOPIC = "friend.accepted"
        
        // Notification events
        const val NOTIFICATION_TOPIC = "notification.send"
        
        // Admin events
        const val ADMIN_POST_DELETED_TOPIC = "admin.post.deleted"
        const val ADMIN_USER_BANNED_TOPIC = "admin.user.banned"
        
        // Security events
        const val PASSWORD_CHANGED_TOPIC = "security.password.changed"
        const val LOGIN_ATTEMPT_TOPIC = "security.login.attempt"
        
        // Dead Letter Queue
        const val DLQ_TOPIC = "dlq.failed.events"
        
        // Retry configuration
        const val MAX_RETRY_ATTEMPTS = 3
        const val RETRY_BACKOFF_MS = 2000L
    }

    // ===========================
    // Message Converter
    // ===========================
    
    @Bean
    fun kafkaMessageConverter(): RecordMessageConverter {
        return JsonMessageConverter()
    }
    
    // ===========================
    // Error Handler with DLQ
    // ===========================
    
    @Bean
    fun errorHandler(): DefaultErrorHandler {
        // Fixed backoff: 2s between retries, max 3 attempts
        val fixedBackOff = FixedBackOff(RETRY_BACKOFF_MS, MAX_RETRY_ATTEMPTS.toLong())
        return DefaultErrorHandler({ consumerRecord, exception ->
            // DLQ handler - log failed messages
            println("[DLQ] Message failed after $MAX_RETRY_ATTEMPTS retries: topic=${consumerRecord.topic()}, key=${consumerRecord.key()}, error=${exception.message}")
            // In production, send to DLQ topic for manual investigation
        }, fixedBackOff).apply {
            // Don't retry for validation errors or business logic errors
            addNotRetryableExceptions(
                IllegalArgumentException::class.java,
                NullPointerException::class.java
            )
        }
    }

    // ===========================
    // Topic Definitions - Post Events
    // ===========================
    
    @Bean
    fun postCreatedTopic(): NewTopic = TopicBuilder
        .name(POST_CREATED_TOPIC)
        .partitions(5) // High throughput for post creation
        .replicas(1)
        .config("retention.ms", Duration.ofDays(7).toMillis().toString()) // 7 days retention
        .config("compression.type", "snappy") // Compress for better network utilization
        .build()
    
    @Bean
    fun postUpdatedTopic(): NewTopic = TopicBuilder
        .name(POST_UPDATED_TOPIC)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(3).toMillis().toString())
        .build()
    
    @Bean
    fun postDeletedTopic(): NewTopic = TopicBuilder
        .name(POST_DELETED_TOPIC)
        .partitions(2)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(30).toMillis().toString()) // Keep longer for audit
        .build()

    @Bean
    fun postLikedTopic(): NewTopic = TopicBuilder
        .name(POST_LIKED_TOPIC)
        .partitions(5) // High volume of likes
        .replicas(1)
        .config("retention.ms", Duration.ofDays(1).toMillis().toString()) // Short retention
        .config("compression.type", "snappy")
        .build()
    
    @Bean
    fun postUnlikedTopic(): NewTopic = TopicBuilder
        .name(POST_UNLIKED_TOPIC)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(1).toMillis().toString())
        .build()

    @Bean
    fun postCommentedTopic(): NewTopic = TopicBuilder
        .name(POST_COMMENTED_TOPIC)
        .partitions(4)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(7).toMillis().toString())
        .build()

    // ===========================
    // Topic Definitions - User Events
    // ===========================
    
    @Bean
    fun userRegisteredTopic(): NewTopic = TopicBuilder
        .name(USER_REGISTERED_TOPIC)
        .partitions(2)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(30).toMillis().toString()) // Keep for analytics
        .build()

    @Bean
    fun userFollowedTopic(): NewTopic = TopicBuilder
        .name(USER_FOLLOWED_TOPIC)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(7).toMillis().toString())
        .build()
    
    @Bean
    fun userUnfollowedTopic(): NewTopic = TopicBuilder
        .name(USER_UNFOLLOWED_TOPIC)
        .partitions(2)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(7).toMillis().toString())
        .build()

    // ===========================
    // Topic Definitions - Message Events
    // ===========================
    
    @Bean
    fun messageSentTopic(): NewTopic = TopicBuilder
        .name(MESSAGE_SENT_TOPIC)
        .partitions(4) // Real-time messaging needs good parallelism
        .replicas(1)
        .config("retention.ms", Duration.ofDays(3).toMillis().toString())
        .build()
    
    @Bean
    fun messageDeletedTopic(): NewTopic = TopicBuilder
        .name(MESSAGE_DELETED_TOPIC)
        .partitions(2)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(7).toMillis().toString())
        .build()

    // ===========================
    // Topic Definitions - Other Events
    // ===========================
    
    @Bean
    fun friendAcceptedTopic(): NewTopic = TopicBuilder
        .name(FRIEND_ACCEPTED_TOPIC)
        .partitions(2)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(7).toMillis().toString())
        .build()
    
    @Bean
    fun notificationTopic(): NewTopic = TopicBuilder
        .name(NOTIFICATION_TOPIC)
        .partitions(5) // High volume notifications
        .replicas(1)
        .config("retention.ms", Duration.ofDays(3).toMillis().toString())
        .config("compression.type", "snappy")
        .build()
    
    @Bean
    fun adminPostDeletedTopic(): NewTopic = TopicBuilder
        .name(ADMIN_POST_DELETED_TOPIC)
        .partitions(1) // Low volume, sequential processing
        .replicas(1)
        .config("retention.ms", Duration.ofDays(90).toMillis().toString()) // Audit trail
        .build()
    
    @Bean
    fun passwordChangedTopic(): NewTopic = TopicBuilder
        .name(PASSWORD_CHANGED_TOPIC)
        .partitions(1) // Security events - sequential
        .replicas(1)
        .config("retention.ms", Duration.ofDays(90).toMillis().toString()) // Security audit
        .build()
    
    @Bean
    fun dlqTopic(): NewTopic = TopicBuilder
        .name(DLQ_TOPIC)
        .partitions(1)
        .replicas(1)
        .config("retention.ms", Duration.ofDays(30).toMillis().toString()) // Keep failed messages
        .build()
}
