package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Standard error response DTOs
 * Sử dụng cho GlobalExceptionHandler
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class ErrorResponse(
    @JsonProperty("success")
    val success: Boolean = false,
    
    @JsonProperty("status")
    val status: Int,
    
    @JsonProperty("error")
    val error: String,
    
    @JsonProperty("message")
    val message: String,
    
    @JsonProperty("path")
    val path: String? = null,
    
    @JsonProperty("timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    @JsonProperty("details")
    val details: Map<String, String>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ValidationErrorResponse(
    @JsonProperty("success")
    val success: Boolean = false,
    
    @JsonProperty("status")
    val status: Int = 400,
    
    @JsonProperty("error")
    val error: String = "Validation Error",
    
    @JsonProperty("message")
    val message: String,
    
    @JsonProperty("timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    @JsonProperty("fieldErrors")
    val fieldErrors: Map<String, String> = emptyMap()
)
