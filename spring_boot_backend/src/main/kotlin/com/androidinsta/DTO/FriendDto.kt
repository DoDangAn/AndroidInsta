package com.androidinsta.dto

import com.androidinsta.Model.FriendRequestStatus
import java.time.LocalDateTime

data class FriendRequestResponse(
    val id: Long,
    val senderId: Long,
    val senderUsername: String,
    val senderFullName: String?,
    val senderAvatarUrl: String?,
    val receiverId: Long,
    val receiverUsername: String,
    val receiverFullName: String?,
    val receiverAvatarUrl: String?,
    val status: FriendRequestStatus,
    val createdAt: LocalDateTime,
    val respondedAt: LocalDateTime?
)

data class FriendResponse(
    val id: Long,
    val userId: Long,
    val username: String,
    val fullName: String?,
    val avatarUrl: String?,
    val bio: String?,
    val mutualFriendsCount: Long,
    val friendsSince: LocalDateTime
)

data class FriendSuggestionResponse(
    val userId: Long,
    val username: String,
    val fullName: String?,
    val avatarUrl: String?,
    val bio: String?,
    val mutualFriendsCount: Long
)

data class FriendshipStatusResponse(
    val isFriend: Boolean,
    val hasPendingRequest: Boolean,
    val pendingRequestSentByMe: Boolean,
    val friendRequestId: Long?
)

data class SendFriendRequestRequest(
    val receiverId: Long
)
