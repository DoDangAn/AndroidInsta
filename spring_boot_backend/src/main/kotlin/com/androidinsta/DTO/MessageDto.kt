package com.androidinsta.dto

import com.androidinsta.Model.Message
import java.time.LocalDateTime

/**
 * DTO cho Message
 */
data class MessageDto(
    val id: Long,
    val content: String?,
    val mediaUrl: String?,
    val messageType: String,
    val sender: UserSummaryDto,
    val receiver: UserSummaryDto,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)

/**
 * Request để gửi message
 */
data class SendMessageRequest(
    val receiverId: Long,
    val content: String?,
    val mediaUrl: String?,
    val messageType: String = "TEXT"
)

/**
 * DTO cho Conversation (danh sách chat)
 */
data class ConversationDto(
    val user: UserSummaryDto,
    val lastMessage: MessageDto?,
    val unreadCount: Int
)

/**
 * Response cho danh sách conversations
 */
data class ConversationsResponse(
    val conversations: List<ConversationDto>
)

/**
 * Response cho chat history
 */
data class ChatHistoryResponse(
    val messages: List<MessageDto>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Long
)

/**
 * Extension function: Convert Message entity sang MessageDto
 */
fun Message.toDto(): MessageDto {
    return MessageDto(
        id = this.id,
        content = this.content,
        mediaUrl = this.mediaUrl,
        messageType = this.messageType.name,
        sender = UserSummaryDto(
            id = this.sender.id,
            username = this.sender.username,
            fullName = this.sender.fullName,
            avatarUrl = this.sender.avatarUrl
        ),
        receiver = UserSummaryDto(
            id = this.receiver.id,
            username = this.receiver.username,
            fullName = this.receiver.fullName,
            avatarUrl = this.receiver.avatarUrl
        ),
        isRead = this.isRead,
        createdAt = this.createdAt
    )
}
