package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Search response DTOs
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchResponse(
    @JsonProperty("users") val users: List<UserSearchResult> = emptyList(),
    @JsonProperty("posts") val posts: List<PostDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserSearchResult(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("isVerified") val isVerified: Boolean = false,
    @JsonProperty("followersCount") val followersCount: Long = 0,
    @JsonProperty("isFollowing") val isFollowing: Boolean = false
)
