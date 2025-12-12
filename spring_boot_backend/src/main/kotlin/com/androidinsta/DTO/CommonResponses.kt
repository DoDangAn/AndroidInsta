package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Standard response DTOs - Cache-safe design
 * Principles:
 * - No generic types (no Any, no T)
 * - Explicit @JsonProperty for all fields
 * - @JsonIgnoreProperties for backward compatibility
 * - Simple types only (String, Long, Boolean)
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class CountResponse(
    @JsonProperty("success")
    val success: Boolean = true,
    
    @JsonProperty("count")
    val count: Long = 0,
    
    @JsonProperty("message")
    val message: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageResponse(
    @JsonProperty("message")
    val message: String,
    
    @JsonProperty("success")
    val success: Boolean = true
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FollowResponse(
    @JsonProperty("success")
    val success: Boolean,
    
    @JsonProperty("message")
    val message: String,
    
    @JsonProperty("isFollowing")
    val isFollowing: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FollowStatusResponse(
    @JsonProperty("success")
    val success: Boolean,
    
    @JsonProperty("isFollowing")
    val isFollowing: Boolean,
    
    @JsonProperty("isFollower")
    val isFollower: Boolean
)
