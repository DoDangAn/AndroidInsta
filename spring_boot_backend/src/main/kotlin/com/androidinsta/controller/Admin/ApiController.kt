package com.androidinsta.controller

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["*"]) // Allow Flutter app to access
class ApiController {

    @GetMapping("/hello")
    fun hello(): Map<String, String> {
        return mapOf("message" to "Hello from Spring Boot with Kotlin!")
    }

}

data class PostRequest(
    val title: String,
    val content: String
)