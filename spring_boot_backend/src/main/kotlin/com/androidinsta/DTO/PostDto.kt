package com.androidinsta.dto

import com.androidinsta.Model.Post
import java.time.LocalDateTime

/**
 * DTO cho Post Response
 */
data class PostDto(
    val id: Long,
    val caption: String,
    val visibility: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val user: UserSummaryDto,
    val mediaFiles: List<MediaFileDto>,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false
)

/**
 * Request DTO để tạo Post mới
 */
data class CreatePostRequest(
    val caption: String,
    val visibility: String = "PUBLIC",
    val mediaUrls: List<String> = emptyList()
)

/**
 * Response DTO cho Feed
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
        caption = this.caption ?: "",
        visibility = this.visibility.name,
        createdAt = this.createdAt!!,
        updatedAt = this.updatedAt ?: this.createdAt!!,
        user = UserSummaryDto(
            id = this.user.id,
            username = this.user.username,
            fullName = this.user.fullName,
            avatarUrl = this.user.avatarUrl
        ),
        mediaFiles = this.mediaFiles.map { 
            MediaFileDto(
                fileUrl = it.fileUrl,
                fileType = it.fileType.name,
                orderIndex = it.orderIndex
            )
        },
        likesCount = this.likes.size,
        commentsCount = this.comments.size,
        isLiked = currentUserId?.let { userId ->
            this.likes.any { it.user.id == userId }
        } ?: false
    )
}
