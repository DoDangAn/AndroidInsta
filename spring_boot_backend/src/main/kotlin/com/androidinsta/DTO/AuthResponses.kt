package com.androidinsta.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Auth response DTOs - Login, Register, Token refresh
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String,
    @JsonProperty("data") val data: JwtResponse? = null
)

// Note: AuthData is deprecated, replaced by JwtResponse from AuthDto.kt
// Keeping this for backward compatibility during migration
@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthData(
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("avatarUrl") val avatarUrl: String?,
    @JsonProperty("accessToken") val accessToken: String,
    @JsonProperty("refreshToken") val refreshToken: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenRefreshResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("data") val data: JwtResponse? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogoutResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PasswordChangeResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurrentUserResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("data") val data: CurrentUserData? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurrentUserData(
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("username") val username: String? = null,
    @JsonProperty("isAuthenticated") val isAuthenticated: Boolean = false
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenValidationResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String,
    @JsonProperty("isAuthenticated") val isAuthenticated: Boolean = false,
    @JsonProperty("userId") val userId: Long? = null
)
