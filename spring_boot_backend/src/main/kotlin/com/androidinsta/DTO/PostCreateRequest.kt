package com.androidinsta.DTO

import com.androidinsta.model.Visibility

data class PostCreateRequest(
    val userId: Long,            // đây là trường bắt buộc
    val caption: String?,
    val visibility: Visibility? = null
)
