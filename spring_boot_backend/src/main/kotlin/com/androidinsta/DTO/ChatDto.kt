package com.androidinsta.dto

import java.time.LocalDateTime

/**
 * Chat history response
 */
data class ChatHistoryResponse(
    val messages: List<MessageDto>,
    val currentPage: Int,
    val totalPages: Int,
    val totalMessages: Long
)
