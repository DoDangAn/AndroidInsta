package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Message/Chat response DTOs
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class SendMessageResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("data") val data: MessageDto? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConversationsResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("data") val data: List<ConversationDto>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConversationDto(
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("lastMessage") val lastMessage: String?,
    @JsonProperty("lastMessageTime") val lastMessageTime: java.time.LocalDateTime?,
    @JsonProperty("unreadCount") val unreadCount: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessagesResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("data") val data: MessagesData
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessagesData(
    @JsonProperty("messages") val messages: List<MessageDto>,
    @JsonProperty("currentPage") val currentPage: Int,
    @JsonProperty("totalPages") val totalPages: Int,
    @JsonProperty("totalItems") val totalItems: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UnreadCountResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("data") val data: UnreadCountData
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UnreadCountData(
    @JsonProperty("count") val count: Long
)
