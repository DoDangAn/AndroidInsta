package com.androidinsta.dto

import com.androidinsta.Model.Message
import com.androidinsta.Model.MessageType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageDto(
    @JsonProperty("id") val id: Long,
    @JsonProperty("senderId") val senderId: Long,
    @JsonProperty("senderUsername") val senderUsername: String,
    @JsonProperty("senderAvatarUrl") val senderAvatarUrl: String?,
    @JsonProperty("receiverId") val receiverId: Long,
    @JsonProperty("receiverUsername") val receiverUsername: String,
    @JsonProperty("receiverAvatarUrl") val receiverAvatarUrl: String?,
    @JsonProperty("content") val content: String?,
    @JsonProperty("mediaUrl") val mediaUrl: String?,
    @JsonProperty("messageType") val messageType: MessageType,
    @JsonProperty("isRead") val isRead: Boolean,
    @JsonProperty("createdAt") val createdAt: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SendMessageRequest(
    @field:jakarta.validation.constraints.NotNull(message = "Receiver ID is required")
    @field:jakarta.validation.constraints.Positive(message = "Receiver ID must be positive")
    val receiverId: Long,
    
    @field:jakarta.validation.constraints.Size(max = 5000, message = "Content must not exceed 5000 characters")
    val content: String?,
    
    val mediaUrl: String? = null,
    val messageType: String = "text"
)

/**
 * Extension function: Convert Message to MessageDto
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
        createdAt = this.createdAt.toString()
    )
}
