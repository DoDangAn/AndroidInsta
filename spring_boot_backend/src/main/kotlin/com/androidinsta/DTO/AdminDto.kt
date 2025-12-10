package com.androidinsta.dto

import com.androidinsta.Model.User
import java.time.LocalDateTime

/**
 * DTO for admin user list view
 */
data class AdminUserDto(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val avatarUrl: String?,
    val isVerified: Boolean,
    val isActive: Boolean,
    val roleName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
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
data class AdminUserDetailDto(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val bio: String?,
    val avatarUrl: String?,
    val isVerified: Boolean,
    val isActive: Boolean,
    val roleName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int
)

/**
 * DTO for user statistics
 */
data class AdminUserStatsDto(
    val userId: Long,
    val username: String,
    val postsCount: Long,
    val followersCount: Long,
    val followingCount: Long,
    val likesGivenCount: Long,
    val likesReceivedCount: Long,
    val commentsGivenCount: Long,
    val commentsReceivedCount: Long
)

/**
 * DTO for system overview statistics
 */
data class AdminOverviewStatsDto(
    val totalUsers: Long,
    val activeUsers: Long,
    val verifiedUsers: Long,
    val totalPosts: Long,
    val totalLikes: Long,
    val totalComments: Long,
    val totalFollows: Long,
    val newUsersToday: Long,
    val newPostsToday: Long
)

/**
 * DTO for time-based statistics
 */
data class AdminTimeStatsDto(
    val period: String,
    val data: List<AdminTimeDataPoint>
)

data class AdminTimeDataPoint(
    val date: String,
    val count: Long
)

/**
 * DTO for top users
 */
data class AdminTopUserDto(
    val id: Long,
    val username: String,
    val fullName: String?,
    val avatarUrl: String?,
    val isVerified: Boolean,
    val count: Long,
    val type: String // "followers", "posts", "likes"
)

/**
 * DTO for top posts
 */
data class AdminTopPostDto(
    val id: Long,
    val caption: String?,
    val username: String,
    val userId: Long,
    val likesCount: Long,
    val commentsCount: Long,
    val createdAt: LocalDateTime
)
