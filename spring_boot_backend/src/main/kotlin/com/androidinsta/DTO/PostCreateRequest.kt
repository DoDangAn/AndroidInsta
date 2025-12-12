package com.androidinsta.dto

import com.androidinsta.Model.Visibility
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class PostCreateRequest(
    @field:NotNull(message = "User ID is required")
    @field:Positive(message = "User ID must be positive")
    val userId: Long,
    
    @field:Size(max = 2200, message = "Caption must not exceed 2200 characters")
    val caption: String?,
    
    val visibility: Visibility? = Visibility.PUBLIC
)
