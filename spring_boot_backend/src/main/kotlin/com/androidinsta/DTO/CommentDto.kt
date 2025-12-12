package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommentRequest(
    @field:NotBlank(message = "Comment content is required")
    @field:Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    @JsonProperty("content") val content: String,
    
    @JsonProperty("parentCommentId") val parentCommentId: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommentResponse(
    @JsonProperty("id") val id: Long,
    @JsonProperty("postId") val postId: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("userAvatarUrl") val userAvatarUrl: String?,
    @JsonProperty("content") val content: String,
    @JsonProperty("parentCommentId") val parentCommentId: Long?,
    @JsonProperty("repliesCount") val repliesCount: Int,
    @JsonProperty("createdAt") val createdAt: LocalDateTime,
    @JsonProperty("replies") val replies: List<CommentResponse>? = null
)
