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
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)

// Update DTO
data class UpdateUserRequest(
    val username: String?,
    val email: String?,
    val password: String?
)