package com.androidinsta.dto

import com.androidinsta.Model.Post
import com.androidinsta.Model.Visibility
import java.time.LocalDateTime

/**
 * DTO for Post detail response
 */
data class PostDto(
    val id: Long,
    val userId: Long,
    val username: String,
    val userAvatarUrl: String?,
    val caption: String?,
    val visibility: Visibility,
    val mediaFiles: List<MediaFileDto>,
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val isLiked: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)

data class CreatePostRequest(
    val caption: String,
    val visibility: String = "PUBLIC",
    val mediaUrls: List<String> = emptyList()
)

/**
 * Feed response with pagination
 */
data class FeedResponse(
    val posts: List<PostDto>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Long
)

/**
 * Extension function: Convert Post entity sang PostDto
 */
fun Post.toDto(currentUserId: Long? = null): PostDto {
    return PostDto(
        id = this.id,
        userId = this.user.id,
        username = this.user.username,
        userAvatarUrl = this.user.avatarUrl,
        caption = this.caption,
        visibility = this.visibility,
        mediaFiles = this.mediaFiles.map { m ->
            MediaFileDto(
                fileUrl = m.fileUrl,
                fileType = m.fileType.name,
                orderIndex = m.orderIndex,
                duration = m.duration,
                thumbnailUrl = m.thumbnailUrl
            )
        },
        likeCount = this.likes.size.toLong(),
        commentCount = this.comments.size.toLong(),
        isLiked = currentUserId?.let { userId ->
            this.likes.any { it.user.id == userId }
        } ?: false,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

data class MediaFileDto(
    val fileUrl: String,
    val fileType: String,
    val orderIndex: Int,
    val duration: Int? = null,
    val thumbnailUrl: String? = null
)
