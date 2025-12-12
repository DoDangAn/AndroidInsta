package com.androidinsta.dto

import com.androidinsta.Model.Visibility
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import com.androidinsta.Model.Post

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostUserResponse(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostMediaFile(
    @JsonProperty("fileUrl") val fileUrl: String,
    @JsonProperty("fileType") val fileType: String,
    @JsonProperty("orderIndex") val orderIndex: Int,
    @JsonProperty("duration") val duration: Int? = null,
    @JsonProperty("thumbnailUrl") val thumbnailUrl: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostResponse(
    @JsonProperty("id") val id: Long,
    @JsonProperty("user") val user: PostUserResponse,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("visibility") val visibility: Visibility,
    @JsonProperty("mediaFiles") val mediaFiles: List<PostMediaFile> = emptyList(),
    @JsonProperty("likeCount") val likeCount: Long = 0,
    @JsonProperty("commentCount") val commentCount: Long = 0,
    @JsonProperty("isLiked") val isLiked: Boolean = false,
    @JsonProperty("createdAt") val createdAt: LocalDateTime?,
    @JsonProperty("updatedAt") val updatedAt: LocalDateTime?
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
