package com.androidinsta.dto

import com.androidinsta.Model.Message
import com.androidinsta.Model.MessageType
import java.time.LocalDateTime

data class MessageDto(
    val id: Long,
    val senderId: Long,
    val senderUsername: String,
    val senderAvatarUrl: String?,
    val receiverId: Long,
    val receiverUsername: String,
    val receiverAvatarUrl: String?,
    val content: String?,
    val mediaUrl: String?,
    val messageType: MessageType,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)

data class SendMessageRequest(
    val receiverId: Long,
    val content: String?,
    val mediaUrl: String? = null,
    val messageType: String = "text"
)

data class ConversationDto(
    val userId: Long,
    val username: String,
    val avatarUrl: String?,
    val lastMessage: String?,
    val lastMessageTime: LocalDateTime?,
    val unreadCount: Long
)

/**
 * Extension function: Convert Message entity to MessageDto
 */
fun Message.toDto(): MessageDto {
    return MessageDto(
        id = this.id,
        senderId = this.sender.id,
        senderUsername = this.sender.username,
        senderAvatarUrl = this.sender.avatarUrl,
        receiverId = this.receiver.id,
        receiverUsername = this.receiver.username,
        receiverAvatarUrl = this.receiver.avatarUrl,
        content = this.content,
        mediaUrl = this.mediaUrl,
        messageType = this.messageType,
        isRead = this.isRead,
        createdAt = this.createdAt
    )
}
