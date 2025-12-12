package com.androidinsta.controller.Admin

import org.springframework.web.bind.annotation.*

/**
 * Basic API controller for testing
 * Simple hello endpoint to verify backend is running
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["*"])
class ApiController {

    /**
     * Hello endpoint for testing
     * GET /api/hello
     */
    @GetMapping("/hello")
    fun hello(): Map<String, String> {
        return mapOf("message" to "Hello from Spring Boot with Kotlin!")
    }
}

data class PostRequest(
    val title: String,
    val content: String
)