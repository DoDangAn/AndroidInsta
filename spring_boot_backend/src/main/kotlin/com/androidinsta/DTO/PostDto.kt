package com.androidinsta.dto

import com.androidinsta.Model.Post
import com.androidinsta.Model.Visibility
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import java.time.LocalDateTime

/**
 * DTO for Post detail response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PostDto(
    @JsonProperty("id") val id: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("userAvatarUrl") val userAvatarUrl: String?,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("visibility") val visibility: Visibility,
    @JsonProperty("mediaFiles") val mediaFiles: List<MediaFileDto>,
    @JsonProperty("likeCount") val likeCount: Long = 0,
    @JsonProperty("commentCount") val commentCount: Long = 0,
    @JsonProperty("isLiked") val isLiked: Boolean = false,
    @JsonProperty("createdAt") val createdAt: String?,
    @JsonProperty("updatedAt") val updatedAt: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreatePostRequest(
    @JsonProperty("caption") val caption: String,
    @JsonProperty("visibility") val visibility: String = "PUBLIC",
    @JsonProperty("mediaUrls") val mediaUrls: List<String> = emptyList()
)

/**
 * Feed response with pagination
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedResponse(
    @JsonProperty("posts") val posts: List<PostDto>,
    @JsonProperty("currentPage") val currentPage: Int,
    @JsonProperty("totalPages") val totalPages: Int,
    @JsonProperty("totalItems") val totalItems: Long
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
        createdAt = this.createdAt?.toString(),
        updatedAt = this.updatedAt?.toString()
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaFileDto(
    @JsonProperty("fileUrl") val fileUrl: String,
    @JsonProperty("fileType") val fileType: String,
    @JsonProperty("orderIndex") val orderIndex: Int,
    @JsonProperty("duration") val duration: Int? = null,
    @JsonProperty("thumbnailUrl") val thumbnailUrl: String? = null
)

// Add Redis serializer for FeedResponse
val feedResponseSerializer: RedisSerializer<FeedResponse> = Jackson2JsonRedisSerializer(FeedResponse::class.java)
