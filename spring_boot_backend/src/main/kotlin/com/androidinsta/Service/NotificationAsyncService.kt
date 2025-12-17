package com.androidinsta.Service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import com.androidinsta.Model.NotificationType

@Service
class NotificationAsyncService(
    private val notificationService: NotificationService
) {
    @Async("taskExecutor")
    fun sendNewPostNotificationAsync(receiverId: Long, senderId: Long, postId: Long) {
        try {
            notificationService.createNotification(
                receiverId = receiverId,
                senderId = senderId,
                type = NotificationType.NEW_POST,
                entityId = postId,
                message = null
            )
        } catch (e: Exception) {
            println("Async notification error for receiver $receiverId: ${'$'}{e.message}")
        }
    }
}
