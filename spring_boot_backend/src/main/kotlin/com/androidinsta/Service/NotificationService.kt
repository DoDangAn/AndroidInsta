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
import java.time.LocalDateTime

@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val kafkaTemplate: KafkaTemplate<String, NotificationEvent>,
    private val messagingTemplate: SimpMessagingTemplate
) {

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
        kafkaTemplate.send(NOTIFICATION_TOPIC, event)
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
                entityId = event.entityId,
                message = event.message
            )

            val saved = notificationRepository.save(notification)

            // Gửi real-time notification qua WebSocket
            val response = toNotificationResponse(saved)
            messagingTemplate.convertAndSendToUser(
                event.receiverId.toString(),
                "/queue/notifications",
                response
            )
        } catch (e: Exception) {
            println("Error handling notification: ${e.message}")
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
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId)
    }

    /**
     * Đánh dấu notification là đã đọc
     */
    fun markAsRead(notificationId: Long, userId: Long) {
        notificationRepository.markAsRead(notificationId, userId)
    }

    /**
     * Đánh dấu tất cả là đã đọc
     */
    fun markAllAsRead(userId: Long) {
        notificationRepository.markAllAsRead(userId)
    }

    /**
     * Xóa notification
     */
    fun deleteNotification(notificationId: Long, userId: Long) {
        val notification = notificationRepository.findById(notificationId).orElse(null)
        if (notification?.receiver?.id == userId) {
            notificationRepository.delete(notification)
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
            message = notification.message,
            entityId = notification.entityId,
            isRead = notification.isRead,
            createdAt = notification.createdAt
        )
    }
}
