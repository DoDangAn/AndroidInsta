package com.androidinsta.controller.user

import com.androidinsta.model.Post
import com.androidinsta.model.Visibility
import com.androidinsta.service.user.PostService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user/posts")
class UserPostController(private val postService: PostService) {

    @GetMapping
    fun getAllPosts() = postService.getAllPosts()

    @GetMapping("/user/{userId}")
    fun getPostsByUser(@PathVariable userId: Long) = postService.getPostsByUser(userId)

    @GetMapping("/{postId}")
    fun getPostById(@PathVariable postId: Long) = postService.getPostById(postId)

    @PostMapping
    fun createPost(@RequestBody post: Post) = postService.createPost(post)

    @PutMapping("/{postId}")
    fun updatePost(@PathVariable postId: Long,
                   @RequestParam userId: Long,
                   @RequestBody request: PostUpdateRequest) =
        postService.updatePost(postId, userId, request.caption, request.visibility)

    @DeleteMapping("/{postId}")
    fun deletePost(@PathVariable postId: Long,
                   @RequestParam userId: Long) =
        postService.deletePost(postId, userId)
}

data class PostUpdateRequest(
    val caption: String?,
    val visibility: Visibility?
)
