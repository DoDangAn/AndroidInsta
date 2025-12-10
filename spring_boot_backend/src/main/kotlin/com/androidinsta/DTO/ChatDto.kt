package com.androidinsta.dto

import java.time.LocalDateTime

/**
 * Conversation item in list
 */
data class ConversationDto(
    val userId: Long,
    val username: String,
    val avatarUrl: String?,
    val fullName: String?,
    val lastMessage: String?,
    val lastMessageTime: LocalDateTime?,
    val unreadCount: Long
)

/**
 * Conversations list response
 */
data class ConversationsResponse(
    val conversations: List<ConversationDto>
)

/**
 * Chat history response
 */
data class ChatHistoryResponse(
    val messages: List<MessageDto>,
    val currentPage: Int,
    val totalPages: Int,
    val totalMessages: Long
)
