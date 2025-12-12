package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * DTO for user statistics
 * Cache-safe: No generic types, no @class metadata
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserStatsDto(
    @JsonProperty("followersCount")
    val followersCount: Long = 0,
    
    @JsonProperty("followingCount")
    val followingCount: Long = 0,
    
    @JsonProperty("postsCount")
    val postsCount: Long = 0
)
