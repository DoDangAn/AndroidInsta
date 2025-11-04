package com.androidinsta.dto

import com.androidinsta.Model.Visibility

data class PostCreateRequest(
    val userId: Long,            // đây là trường bắt buộc
    val caption: String?,
    val visibility: Visibility? = null
)
