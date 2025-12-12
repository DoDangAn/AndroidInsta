package com.androidinsta.dto

import com.androidinsta.Model.MediaType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

// Note: UserSearchResult moved to SearchResponses.kt to avoid duplication

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostSearchResult(
    @JsonProperty("id") val id: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("userAvatarUrl") val userAvatarUrl: String?,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("mediaFiles") val mediaFiles: List<MediaFileInfo>,
    @JsonProperty("likeCount") val likeCount: Long = 0,
    @JsonProperty("commentCount") val commentCount: Long = 0,
    @JsonProperty("createdAt") val createdAt: LocalDateTime?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaFileInfo(
    @JsonProperty("fileUrl") val fileUrl: String,
    @JsonProperty("fileType") val fileType: MediaType,
    @JsonProperty("thumbnailUrl") val thumbnailUrl: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TagSearchResult(
    @JsonProperty("id") val id: Long,
    @JsonProperty("name") val name: String,
    @JsonProperty("postsCount") val postsCount: Long = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchAllResult(
    @JsonProperty("users") val users: List<UserSearchResult>,
    @JsonProperty("posts") val posts: List<PostSearchResult>,
    @JsonProperty("tags") val tags: List<TagSearchResult>
)
