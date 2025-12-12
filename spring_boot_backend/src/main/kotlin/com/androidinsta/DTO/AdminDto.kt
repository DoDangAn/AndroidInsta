package com.androidinsta.dto

import com.androidinsta.Model.User
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * DTO for admin user list view
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminUserDto(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("isVerified") val isVerified: Boolean,
    @JsonProperty("isActive") val isActive: Boolean,
    @JsonProperty("roleName") val roleName: String,
    @JsonProperty("createdAt") val createdAt: LocalDateTime,
    @JsonProperty("updatedAt") val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromUser(user: User): AdminUserDto {
            return AdminUserDto(
                id = user.id,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                avatarUrl = user.avatarUrl,
                isVerified = user.isVerified,
                isActive = user.isActive,
                roleName = user.role?.name ?: "USER",
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }

        fun fromUserWithDetails(user: User): AdminUserDetailDto {
            return AdminUserDetailDto(
                id = user.id,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                bio = user.bio,
                avatarUrl = user.avatarUrl,
                isVerified = user.isVerified,
                isActive = user.isActive,
                roleName = user.role?.name ?: "USER",
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
                postsCount = user.posts.size,
                followersCount = user.followers.size,
                followingCount = user.following.size
            )
        }
    }
}

/**
 * DTO for admin user detail view with statistics
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminUserDetailDto(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("bio") val bio: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("isVerified") val isVerified: Boolean,
    @JsonProperty("isActive") val isActive: Boolean,
    @JsonProperty("roleName") val roleName: String,
    @JsonProperty("createdAt") val createdAt: LocalDateTime,
    @JsonProperty("updatedAt") val updatedAt: LocalDateTime?,
    @JsonProperty("postsCount") val postsCount: Int,
    @JsonProperty("followersCount") val followersCount: Int,
    @JsonProperty("followingCount") val followingCount: Int
)

/**
 * DTO for user statistics
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminUserStatsDto(
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("postsCount") val postsCount: Long,
    @JsonProperty("followersCount") val followersCount: Long,
    @JsonProperty("followingCount") val followingCount: Long,
    @JsonProperty("likesGivenCount") val likesGivenCount: Long,
    @JsonProperty("likesReceivedCount") val likesReceivedCount: Long,
    @JsonProperty("commentsGivenCount") val commentsGivenCount: Long,
    @JsonProperty("commentsReceivedCount") val commentsReceivedCount: Long
)

/**
 * DTO for system overview statistics
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminOverviewStatsDto(
    @JsonProperty("totalUsers") val totalUsers: Long,
    @JsonProperty("activeUsers") val activeUsers: Long,
    @JsonProperty("verifiedUsers") val verifiedUsers: Long,
    @JsonProperty("totalPosts") val totalPosts: Long,
    @JsonProperty("totalLikes") val totalLikes: Long,
    @JsonProperty("totalComments") val totalComments: Long,
    @JsonProperty("totalFollows") val totalFollows: Long,
    @JsonProperty("newUsersToday") val newUsersToday: Long,
    @JsonProperty("newPostsToday") val newPostsToday: Long
)

/**
 * DTO for time-based statistics
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminTimeStatsDto(
    @JsonProperty("period") val period: String,
    @JsonProperty("data") val data: List<AdminTimeDataPoint>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminTimeDataPoint(
    @JsonProperty("date") val date: String,
    @JsonProperty("count") val count: Long
)

/**
 * DTO for top users
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminTopUserDto(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("isVerified") val isVerified: Boolean,
    @JsonProperty("count") val count: Long,
    @JsonProperty("type") val type: String // "followers", "posts", "likes"
)

/**
 * DTO for top posts
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdminTopPostDto(
    @JsonProperty("id") val id: Long,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("username") val username: String,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("likesCount") val likesCount: Long,
    @JsonProperty("commentsCount") val commentsCount: Long,
    @JsonProperty("createdAt") val createdAt: LocalDateTime
)
