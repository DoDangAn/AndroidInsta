package com.androidinsta.DTO

import com.androidinsta.model.Visibility
import java.time.LocalDateTime
import com.androidinsta.model.Post

data class UserResponse(
    val id: Long,
    val username: String
)

data class PostResponse(
    val id: Long,
    val user: UserResponse,
    val caption: String?,
    val visibility: Visibility,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)

// Extension function chuyển từ Post -> PostResponse
fun Post.toPostResponse() = PostResponse(
    id = this.id,
    user = UserResponse(
        id = this.user.id,
        username = this.user.username
    ),
    caption = this.caption,
    visibility = this.visibility,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)
