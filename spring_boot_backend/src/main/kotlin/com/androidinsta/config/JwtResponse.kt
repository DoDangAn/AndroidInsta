package com.androidinsta.config

import java.time.LocalDateTime

data class JwtResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserInfo
)

data class UserInfo(
    val id: Long,
    val username: String,
    val email: String,
    val roles: List<String>
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: JwtResponse? = null
)