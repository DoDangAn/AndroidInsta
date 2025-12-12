package com.androidinsta.dto

import com.androidinsta.Model.User
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.format.DateTimeFormatter

/**
 * User profile và related response DTOs
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserProfileResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("data") val data: UserProfileData?,
    @JsonProperty("message") val message: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserProfileData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("email") val email: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("bio") val bio: String?,
    @JsonProperty("isVerified") val isVerified: Boolean = false,
    @JsonProperty("isActive") val isActive: Boolean = true,
    @JsonProperty("createdAt") val createdAt: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserListResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("data") val data: List<UserProfileData>,
    @JsonProperty("count") val count: Int = 0,
    @JsonProperty("message") val message: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdateProfileResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String,
    @JsonProperty("data") val data: UserProfileData? = null
)

/**
 * Extension function: Convert User entity to UserProfileData DTO
 */
fun User.toProfileData(): UserProfileData {
    return UserProfileData(
        id = this.id,
        username = this.username,
        fullName = this.fullName,
        email = this.email,
        avatarUrl = this.avatarUrl,
        bio = this.bio,
        isVerified = this.isVerified,
        isActive = this.isActive,
        createdAt = this.createdAt.format(DateTimeFormatter.ISO_DATE_TIME)
    )
}

