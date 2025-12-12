package com.androidinsta.dto

import com.androidinsta.Model.NotificationType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationResponse(
    @JsonProperty("id") val id: Long,
    @JsonProperty("senderId") val senderId: Long,
    @JsonProperty("senderUsername") val senderUsername: String,
    @JsonProperty("senderAvatarUrl") val senderAvatarUrl: String?,
    @JsonProperty("type") val type: NotificationType,
    @JsonProperty("message") val message: String?,
    @JsonProperty("entityId") val entityId: Long?,
    @JsonProperty("isRead") val isRead: Boolean,
    @JsonProperty("createdAt") val createdAt: LocalDateTime
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationEvent(
    @JsonProperty("receiverId") val receiverId: Long,
    @JsonProperty("senderId") val senderId: Long,
    @JsonProperty("type") val type: NotificationType,
    @JsonProperty("entityId") val entityId: Long?,
    @JsonProperty("message") val message: String?
)
