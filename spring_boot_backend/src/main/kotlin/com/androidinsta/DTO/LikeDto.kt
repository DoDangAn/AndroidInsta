package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Like response DTOs
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class LikeResponse(
    @JsonProperty("success")
    val success: Boolean,
    
    @JsonProperty("message")
    val message: String,
    
    @JsonProperty("likesCount")
    val likesCount: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LikeStatusResponse(
    @JsonProperty("success")
    val success: Boolean = true,
    
    @JsonProperty("isLiked")
    val isLiked: Boolean
)
