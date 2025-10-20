package com.androidinsta.dto

import com.androidinsta.Model.Post
import com.androidinsta.Model.User

data class ProfileDto(
    val id: Long,
    val username: String,
    val fullName: String?,
    val bio: String?,
    val avatarUrl: String?,
    val posts: List<PostSummaryDto>,
    val followers: List<UserSummaryDto>,
    val following: List<UserSummaryDto>
)

data class PostSummaryDto(
    val id: Long,
    val mediaFiles: List<MediaFileDto>
)

fun User.toProfileDto(): ProfileDto {
    return ProfileDto(
        id = this.id,
        username = this.username,
        fullName = this.fullName,
        bio = this.bio,
        avatarUrl = this.avatarUrl,
        posts = this.posts.map { it.toPostSummaryDto() },
        followers = this.followers.map { it.follower.toUserSummaryDto() },
        following = this.following.map { it.followed.toUserSummaryDto() }
    )
}

fun Post.toPostSummaryDto(): PostSummaryDto {
    return PostSummaryDto(
        id = this.id,
        mediaFiles = this.mediaFiles.map {
            MediaFileDto(
                fileUrl = it.fileUrl,
                fileType = it.fileType.name,
                orderIndex = it.orderIndex
            )
        }
    )
}
