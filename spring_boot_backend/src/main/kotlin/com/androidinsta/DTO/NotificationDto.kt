package com.androidinsta.dto

import com.androidinsta.Model.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val senderId: Long,
    val senderUsername: String,
    val senderAvatarUrl: String?,
    val type: NotificationType,
    val message: String?,
    val entityId: Long?,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)

data class NotificationEvent(
    val receiverId: Long,
    val senderId: Long,
    val type: NotificationType,
    val entityId: Long?,
    val message: String?
)
