package com.androidinsta.dto

import com.androidinsta.Model.FriendRequestStatus
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class FriendRequestResponse(
    @JsonProperty("id") val id: Long,
    @JsonProperty("senderId") val senderId: Long,
    @JsonProperty("senderUsername") val senderUsername: String,
    @JsonProperty("senderFullName") val senderFullName: String?,
    @JsonProperty("senderAvatarUrl") val senderAvatarUrl: String?,
    @JsonProperty("receiverId") val receiverId: Long,
    @JsonProperty("receiverUsername") val receiverUsername: String,
    @JsonProperty("receiverFullName") val receiverFullName: String?,
    @JsonProperty("receiverAvatarUrl") val receiverAvatarUrl: String?,
    @JsonProperty("status") val status: FriendRequestStatus,
    @JsonProperty("createdAt") val createdAt: LocalDateTime,
    @JsonProperty("respondedAt") val respondedAt: LocalDateTime?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FriendResponse(
    @JsonProperty("id") val id: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("bio") val bio: String?,
    @JsonProperty("mutualFriendsCount") val mutualFriendsCount: Long,
    @JsonProperty("friendsSince") val friendsSince: LocalDateTime
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FriendSuggestionResponse(
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("bio") val bio: String?,
    @JsonProperty("mutualFriendsCount") val mutualFriendsCount: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FriendshipStatusResponse(
    @JsonProperty("isFriend") val isFriend: Boolean,
    @JsonProperty("hasPendingRequest") val hasPendingRequest: Boolean,
    @JsonProperty("pendingRequestSentByMe") val pendingRequestSentByMe: Boolean,
    @JsonProperty("friendRequestId") val friendRequestId: Long?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SendFriendRequestRequest(
    @field:jakarta.validation.constraints.NotNull(message = "Receiver ID is required")
    @field:jakarta.validation.constraints.Positive(message = "Receiver ID must be positive")
    @JsonProperty("receiverId") val receiverId: Long
)
