package com.androidinsta.dto

import com.androidinsta.Model.User
import java.time.LocalDateTime

/**
 * User response DTO
 */
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val bio: String?,
    val avatarUrl: String?,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val postsCount: Long = 0,
    val createdAt: LocalDateTime?
)

/**
 * Update user request
 */
data class UpdateUserRequest(
    @field:jakarta.validation.constraints.Email(message = "Email should be valid")
    val email: String?,
    
    @field:jakarta.validation.constraints.Size(max = 100, message = "Full name must not exceed 100 characters")
    val fullName: String?,
    
    @field:jakarta.validation.constraints.Size(max = 500, message = "Bio must not exceed 500 characters")
    val bio: String?,
    
    val avatarUrl: String?
)

/**
 * Extension function: Convert User to UserResponse
 */
fun User.toResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        username = this.username,
        email = this.email,
        fullName = this.fullName,
        bio = this.bio,
        avatarUrl = this.avatarUrl,
        isVerified = this.isVerified,
        isActive = this.isActive,
        followersCount = this.followers.size.toLong(),
        followingCount = this.following.size.toLong(),
        postsCount = this.posts.size.toLong(),
        createdAt = this.createdAt
    )
}
