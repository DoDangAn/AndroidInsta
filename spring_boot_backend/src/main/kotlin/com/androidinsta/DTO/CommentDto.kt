package com.androidinsta.dto

import java.time.LocalDateTime

data class CommentRequest(
    val content: String,
    val parentCommentId: Long? = null
)

data class CommentResponse(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val username: String,
    val userAvatarUrl: String?,
    val content: String,
    val parentCommentId: Long?,
    val repliesCount: Int,
    val createdAt: LocalDateTime,
    val replies: List<CommentResponse>? = null
)
