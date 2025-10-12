package com.androidinsta.DTO

import com.androidinsta.model.Visibility


data class PostUpdateRequest(
    val caption: String?,
    val visibility: Visibility? = null
)