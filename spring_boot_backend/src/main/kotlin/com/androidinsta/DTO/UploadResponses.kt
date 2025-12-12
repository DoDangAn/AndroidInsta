package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Upload response DTOs - chuyên nghiệp, không dùng Map<String, Any>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class UploadResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("url") val url: String? = null,
    @JsonProperty("publicId") val publicId: String? = null,
    @JsonProperty("width") val width: Int? = null,
    @JsonProperty("height") val height: Int? = null,
    @JsonProperty("format") val format: String? = null,
    @JsonProperty("size") val size: Long? = null,
    @JsonProperty("duration") val duration: Int? = null,
    @JsonProperty("thumbnailUrl") val thumbnailUrl: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostUploadResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("postId") val postId: Long? = null,
    @JsonProperty("imageUrls") val imageUrls: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReelUploadResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("reelId") val reelId: Long? = null,
    @JsonProperty("videoUrl") val videoUrl: String? = null,
    @JsonProperty("thumbnailUrl") val thumbnailUrl: String? = null,
    @JsonProperty("duration") val duration: Int? = null
)
