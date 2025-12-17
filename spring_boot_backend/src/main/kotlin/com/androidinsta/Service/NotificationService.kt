package com.androidinsta.Service

import com.androidinsta.Model.Notification
import com.androidinsta.Model.NotificationType
import com.androidinsta.Repository.User.NotificationRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.dto.NotificationEvent
import com.androidinsta.dto.NotificationResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.LocalDateTime

@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val kafkaTemplate: KafkaTemplate<String, NotificationEvent>,
    private val messagingTemplate: SimpMessagingTemplate,
    private val redisService: RedisService,
    private val kafkaProducerService: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)
    private val objectMapper = jacksonObjectMapper().registerKotlinModule()

    companion object {
        const val NOTIFICATION_TOPIC = "notification-events"
    }

    /**
     * Gửi notification event qua Kafka
     */
    fun sendNotification(
        receiverId: Long,
        senderId: Long,
        type: NotificationType,
        entityId: Long?,
        message: String?
    ) {
        val event = NotificationEvent(
            receiverId = receiverId,
            senderId = senderId,
            type = type,
            entityId = entityId,
            message = message
        )

        // Publish after DB transaction commit to avoid sending events for rolled-back work
        try {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronizationAdapter() {
                override fun afterCommit() {
                    try {
                        // Use KafkaProducerService for unified publishing (audit/analytics)
                        kafkaProducerService.sendNotificationEvent(
                            receiverId,
                            "${type.name}",
                            message ?: "",
                            type.name
                        )
                    } catch (ex: Exception) {
                        logger.error("Failed to publish notification event after commit", ex)
                    }
                }
            })
        } catch (e: Exception) {
            logger.error("Failed to register afterCommit for notification event, falling back to immediate send", e)
            try {
                kafkaTemplate.send(NOTIFICATION_TOPIC, event)
            } catch (ex: Exception) {
                logger.error("Fallback immediate kafka send failed", ex)
            }
        }
    }

    /**
     * Xử lý notification event từ Kafka
     */
    @KafkaListener(topics = [NOTIFICATION_TOPIC], groupId = "notification-group")
    fun handleNotificationEvent(event: NotificationEvent) {
        try {
            // Không gửi notification cho chính mình
            if (event.senderId == event.receiverId) {
                return
            }

            val sender = userRepository.findById(event.senderId).orElse(null) ?: return
            val receiver = userRepository.findById(event.receiverId).orElse(null) ?: return

            // Tạo notification trong database
            val notification = Notification(
                sender = sender,
                receiver = receiver,
                type = event.type,
                entityId = event.entityId
            )

            val saved = notificationRepository.save(notification)

            // After commit: update Redis counters/lists and push realtime
            try {
                TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronizationAdapter() {
                    override fun afterCommit() {
                        try {
                            // Increment unread counter
                            redisService.increment("notification:unread:count:${event.receiverId}", 1, java.time.Duration.ofDays(30))

                            // Push recent notification summary into Redis list
                            val summary = mapOf(
                                "id" to saved.id,
                                "senderId" to saved.sender.id,
                                "senderUsername" to saved.sender.username,
                                "senderAvatarUrl" to saved.sender.avatarUrl,
                                "type" to saved.type.name,
                                "entityId" to saved.entityId,
                                "isRead" to saved.isRead,
                                "createdAt" to (saved.createdAt?.toString() ?: "")
                            )
                            val json = objectMapper.writeValueAsString(summary)
                            redisService.pushToList("notifications:recent:${event.receiverId}", json, 200, java.time.Duration.ofDays(30))

                            // Send real-time notification via WebSocket
                            val response = toNotificationResponse(saved)
                            messagingTemplate.convertAndSendToUser(
                                event.receiverId.toString(),
                                "/queue/notifications",
                                response
                            )
                        } catch (ex: Exception) {
                            logger.error("Error in afterCommit handling for notification ${saved.id}", ex)
                        }
                    }
                })
            } catch (ex: Exception) {
                logger.error("Failed to register afterCommit for notification handling", ex)
                // Best-effort immediate actions
                redisService.increment("notification:unread:count:${event.receiverId}", 1, java.time.Duration.ofDays(30))
                val summary = mapOf(
                    "id" to saved.id,
                    "senderId" to saved.sender.id,
                    "senderUsername" to saved.sender.username,
                    "senderAvatarUrl" to saved.sender.avatarUrl,
                    "type" to saved.type.name,
                    "entityId" to saved.entityId,
                    "isRead" to saved.isRead,
                    "createdAt" to (saved.createdAt?.toString() ?: "")
                )
                val json = objectMapper.writeValueAsString(summary)
                redisService.pushToList("notifications:recent:${event.receiverId}", json, 200, java.time.Duration.ofDays(30))
                val response = toNotificationResponse(saved)
                messagingTemplate.convertAndSendToUser(
                    event.receiverId.toString(),
                    "/queue/notifications",
                    response
                )
            }
        } catch (e: Exception) {
            println("Error handling notification: ${e.message}")
        }
    }

    /**
     * Tạo notification trực tiếp (synchronous) — lưu vào DB và gửi realtime.
     * Không publish Kafka để tránh duplicate khi caller muốn immediate persistence.
     */
    fun createNotification(
        receiverId: Long,
        senderId: Long,
        type: NotificationType,
        entityId: Long?,
        message: String?
    ) {
        try {
            if (receiverId == senderId) return

            val sender = userRepository.findById(senderId).orElse(null) ?: return
            val receiver = userRepository.findById(receiverId).orElse(null) ?: return

            val notification = Notification(
                sender = sender,
                receiver = receiver,
                type = type,
                entityId = entityId
            )

            val saved = notificationRepository.save(notification)

            // Register after-commit actions: invalidate caches, push Redis lists, send realtime and publish audit event
            try {
                TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronizationAdapter() {
                    override fun afterCommit() {
                        try {
                            // Increment unread counter
                            redisService.increment("notification:unread:count:$receiverId", 1, java.time.Duration.ofDays(30))

                            // Push recent notification summary
                            val summary = mapOf(
                                "id" to saved.id,
                                "senderId" to saved.sender.id,
                                "senderUsername" to saved.sender.username,
                                "senderAvatarUrl" to saved.sender.avatarUrl,
                                "type" to saved.type.name,
                                "entityId" to saved.entityId,
                                "isRead" to saved.isRead,
                                "createdAt" to (saved.createdAt?.toString() ?: "")
                            )
                            val json = objectMapper.writeValueAsString(summary)
                            redisService.pushToList("notifications:recent:$receiverId", json, 200, java.time.Duration.ofDays(30))

                            // Send real-time notification via WebSocket
                            val response = toNotificationResponse(saved)
                            messagingTemplate.convertAndSendToUser(
                                receiverId.toString(),
                                "/queue/notifications",
                                response
                            )

                            // Publish an audit/analytics event to Kafka for downstream consumers
                            try {
                                kafkaProducerService.sendNotificationEvent(
                                    receiverId,
                                    "${sender.username} - ${type.name}",
                                    message ?: "",
                                    type.name
                                )
                            } catch (e: Exception) {
                                logger.error("Error publishing notification event to Kafka: ${e.message}")
                            }
                        } catch (ex: Exception) {
                            logger.error("Error in afterCommit for createNotification ${saved.id}", ex)
                        }
                    }
                })
            } catch (ex: Exception) {
                logger.error("Failed to register afterCommit for createNotification", ex)
                // Best-effort immediate actions
                redisService.increment("notification:unread:count:$receiverId", 1, java.time.Duration.ofDays(30))
                val summary = mapOf(
                    "id" to saved.id,
                    "senderId" to saved.sender.id,
                    "senderUsername" to saved.sender.username,
                    "senderAvatarUrl" to saved.sender.avatarUrl,
                    "type" to saved.type.name,
                    "entityId" to saved.entityId,
                    "isRead" to saved.isRead,
                    "createdAt" to (saved.createdAt?.toString() ?: "")
                )
                val json = objectMapper.writeValueAsString(summary)
                redisService.pushToList("notifications:recent:$receiverId", json, 200, java.time.Duration.ofDays(30))
                val response = toNotificationResponse(saved)
                messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/notifications",
                    response
                )
                try {
                    kafkaProducerService.sendNotificationEvent(
                        receiverId,
                        "${sender.username} - ${type.name}",
                        message ?: "",
                        type.name
                    )
                } catch (e: Exception) {
                    logger.error("Fallback kafka publish failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error creating notification: ${e.message}")
        }
    }

    /**
     * Lấy danh sách notifications của user
     */
    fun getNotifications(userId: Long, pageable: Pageable): Page<NotificationResponse> {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageable)
            .map { toNotificationResponse(it) }
    }

    /**
     * Lấy unread notifications
     */
    fun getUnreadNotifications(userId: Long, pageable: Pageable): Page<NotificationResponse> {
        return notificationRepository.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
            .map { toNotificationResponse(it) }
    }

    /**
     * Đếm unread notifications
     */
    fun getUnreadCount(userId: Long): Long {
        val cacheKey = "notification:unread:count:$userId"
        val cached = redisService.get(cacheKey, Long::class.java)
        if (cached != null) return cached
        
        val count = notificationRepository.countByReceiverIdAndIsReadFalse(userId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(1))
        return count
    }

    /**
     * Đánh dấu notification là đã đọc
     */
    fun markAsRead(notificationId: Long, userId: Long) {
        notificationRepository.markAsRead(notificationId, userId)
        // Invalidate cache
        redisService.delete("notification:unread:count:$userId")
    }

    /**
     * Đánh dấu tất cả là đã đọc
     */
    fun markAllAsRead(userId: Long) {
        notificationRepository.markAllAsRead(userId)
        // Invalidate cache
        redisService.delete("notification:unread:count:$userId")
    }

    /**
     * Xóa notification
     */
    fun deleteNotification(notificationId: Long, userId: Long) {
        val notification = notificationRepository.findById(notificationId).orElse(null)
        if (notification?.receiver?.id == userId) {
            notificationRepository.delete(notification)
            // Invalidate cache if notification was unread
            if (!notification.isRead) {
                redisService.delete("notification:unread:count:$userId")
            }
        }
    }

    /**
     * Xóa notifications cũ (scheduled task)
     */
    fun deleteOldNotifications() {
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        notificationRepository.deleteOldNotifications(thirtyDaysAgo)
    }

    // Helper functions
    private fun toNotificationResponse(notification: Notification): NotificationResponse {
        return NotificationResponse(
            id = notification.id,
            senderId = notification.sender.id,
            senderUsername = notification.sender.username,
            senderAvatarUrl = notification.sender.avatarUrl,
            type = notification.type,
            message = null, // Database doesn't store message field
            entityId = notification.entityId,
            isRead = notification.isRead,
            createdAt = notification.createdAt
        )
    }
}
