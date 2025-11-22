package com.androidinsta.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class GoogleLoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Google ID is required")
    val googleId: String,

    val fullName: String? = null,
    val photoUrl: String? = null
)
