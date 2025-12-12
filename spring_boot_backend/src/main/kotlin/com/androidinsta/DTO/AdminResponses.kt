package com.androidinsta.dto

import com.androidinsta.Model.User
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Admin response DTOs
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminUserListResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("users") val users: List<AdminUserData>,
    @JsonProperty("currentPage") val currentPage: Int,
    @JsonProperty("totalPages") val totalPages: Int,
    @JsonProperty("totalItems") val totalItems: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminUserData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("isVerified") val isVerified: Boolean,
    @JsonProperty("isActive") val isActive: Boolean,
    @JsonProperty("createdAt") val createdAt: String
) {
    companion object {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        
        fun fromUser(user: User): AdminUserData {
            return AdminUserData(
                id = user.id,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                isVerified = user.isVerified,
                isActive = user.isActive,
                createdAt = user.createdAt.format(formatter)
            )
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminActionResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String,
    @JsonProperty("data") val data: AdminUserData? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminStatsResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("data") val data: Any? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserActivityStatsResponse(
    @JsonProperty("date") val date: String,
    @JsonProperty("newUsers") val newUsers: Long,
    @JsonProperty("activeUsers") val activeUsers: Long,
    @JsonProperty("newPosts") val newPosts: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContentStatsResponse(
    @JsonProperty("totalPosts") val totalPosts: Long,
    @JsonProperty("totalComments") val totalComments: Long,
    @JsonProperty("totalLikes") val totalLikes: Long,
    @JsonProperty("avgPostsPerUser") val avgPostsPerUser: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EngagementStatsResponse(
    @JsonProperty("avgLikesPerPost") val avgLikesPerPost: Double,
    @JsonProperty("avgCommentsPerPost") val avgCommentsPerPost: Double,
    @JsonProperty("topPosts") val topPosts: List<PostEngagementData>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostEngagementData(
    @JsonProperty("postId") val postId: Long,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("likes") val likes: Long,
    @JsonProperty("comments") val comments: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EngagementStatsData(
    @JsonProperty("likes") val likes: AdminTimeStatsDto,
    @JsonProperty("comments") val comments: AdminTimeStatsDto,
    @JsonProperty("follows") val follows: AdminTimeStatsDto
)

