package com.androidinsta.dto

import com.androidinsta.Model.Visibility


data class PostUpdateRequest(
    val caption: String?,
    val visibility: Visibility? = null
)