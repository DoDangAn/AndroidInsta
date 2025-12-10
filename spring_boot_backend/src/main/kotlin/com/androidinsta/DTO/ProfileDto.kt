package com.androidinsta.dto

import com.androidinsta.Model.User
import java.time.LocalDateTime

data class ProfileDto(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val bio: String?,
    val avatarUrl: String?,
    val followersCount: Long,
    val followingCount: Long,
    val postsCount: Long
)

data class PostSummaryDto(
    val id: Long,
    val caption: String?,
    val mediaUrl: String?
)

data class UserSummaryDto(
    val id: Long,
    val username: String,
    val avatarUrl: String?
)

/**
 * Extension function: Convert User to ProfileDto
 */
fun User.toProfileDto(): ProfileDto {
    return ProfileDto(
        id = this.id,
        username = this.username,
        email = this.email,
        fullName = this.fullName,
        bio = this.bio,
        avatarUrl = this.avatarUrl,
        followersCount = this.followers.size.toLong(),
        followingCount = this.following.size.toLong(),
        postsCount = this.posts.size.toLong()
    )
}
