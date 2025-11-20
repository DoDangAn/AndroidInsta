package com.androidinsta.dto

import java.time.LocalDateTime

// Request DTO để tạo user mới
data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String
)

// Response DTO (không có password)
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)

// Update DTO
data class UpdateUserRequest(
    val fullName: String?,
    val bio: String?,
    val email: String?,
    val avatarUrl: String?
)