package com.androidinsta.dto

import com.androidinsta.Model.Visibility
import jakarta.validation.constraints.Size

data class PostUpdateRequest(
    @field:Size(max = 2200, message = "Caption must not exceed 2200 characters")
    val caption: String?,
    
    val visibility: Visibility? = null
)