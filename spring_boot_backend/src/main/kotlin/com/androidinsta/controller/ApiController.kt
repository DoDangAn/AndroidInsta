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

    @PostMapping("/posts")
    fun createPost(@RequestBody post: PostRequest): Map<String, Any> {
        // Simple response - in real app, save to database
        return mapOf(
            "id" to 1,
            "title" to post.title,
            "content" to post.content,
            "status" to "created"
        )
    }

    @GetMapping("/posts")
    fun getPosts(): List<Map<String, Any>> {
        // Sample data - in real app, fetch from database
        return listOf(
            mapOf(
                "id" to 1,
                "title" to "First Post",
                "content" to "This is my first post!",
                "author" to "Admin"
            ),
            mapOf(
                "id" to 2,
                "title" to "Second Post", 
                "content" to "Another great post!",
                "author" to "User"
            )
        )
    }
}

data class PostRequest(
    val title: String,
    val content: String
)