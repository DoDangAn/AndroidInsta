package com.androidinsta.dto

import com.androidinsta.Model.MediaType
import java.time.LocalDateTime

// Search Response DTOs
data class UserSearchResult(
    val id: Long,
    val username: String,
    val fullName: String?,
    val avatarUrl: String?,
    val isVerified: Boolean,
    val followersCount: Long = 0,
    val isFollowing: Boolean = false
)

data class PostSearchResult(
    val id: Long,
    val userId: Long,
    val username: String,
    val userAvatarUrl: String?,
    val caption: String?,
    val mediaFiles: List<MediaFileInfo>,
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val createdAt: LocalDateTime?
)

data class MediaFileInfo(
    val fileUrl: String,
    val fileType: MediaType,
    val thumbnailUrl: String? = null
)

data class TagSearchResult(
    val id: Long,
    val name: String,
    val postsCount: Long = 0
)

data class SearchAllResult(
    val users: List<UserSearchResult>,
    val posts: List<PostSearchResult>,
    val tags: List<TagSearchResult>
)
