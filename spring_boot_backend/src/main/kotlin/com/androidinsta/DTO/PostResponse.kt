package com.androidinsta.dto

import com.androidinsta.Model.Visibility
import java.time.LocalDateTime
import com.androidinsta.Model.Post

data class PostUserResponse(
    val id: Long,
    val username: String
)

data class PostMediaFile(
    val fileUrl: String,
    val fileType: String,
    val orderIndex: Int,
    val duration: Int? = null,
    val thumbnailUrl: String? = null
)

data class PostResponse(
    val id: Long,
    val user: PostUserResponse,
    val caption: String?,
    val visibility: Visibility,
    val mediaFiles: List<PostMediaFile> = emptyList(),
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val isLiked: Boolean = false,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

// Extension function chuyển từ Post -> PostResponse
fun Post.toPostResponse() = PostResponse(
    id = this.id,
    user = PostUserResponse(
        id = this.user.id,
        username = this.user.username
    ),
    caption = this.caption,
    visibility = this.visibility,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)
