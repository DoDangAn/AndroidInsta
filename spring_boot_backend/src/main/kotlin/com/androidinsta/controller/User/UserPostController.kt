package com.androidinsta.controller.User

import com.androidinsta.DTO.PostCreateRequest
import com.androidinsta.DTO.PostUpdateRequest
import com.androidinsta.model.Post
import com.androidinsta.DTO.PostResponse
import com.androidinsta.DTO.toPostResponse
import com.androidinsta.Service.User.PostService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user/posts")
class PostController(private val postService: PostService) {

    // Lấy tất cả post
    @GetMapping
    fun getAllPosts(): List<PostResponse> =
        postService.getAllPosts()

    // Lấy các post của 1 user
    @GetMapping("/user/{userId}")
    fun getPostsByUser(@PathVariable userId: Long): List<PostResponse> =
        postService.getPostsByUser(userId)

    // Lấy 1 post theo postId
    @GetMapping("/{postId}")
    fun getPostById(@PathVariable postId: Long): PostResponse =
        postService.getPostById(postId)

    // Tạo post mới
    @PostMapping
    fun createPost(@RequestBody request: PostCreateRequest): PostResponse {
        val post: Post = postService.createPost(request)
        return post.toPostResponse()
    }



    // Cập nhật post
    @PutMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @RequestParam userId: Long,
        @RequestBody request: PostUpdateRequest
    ): PostResponse =
        postService.updatePost(postId, userId, request.caption, request.visibility)

    // Xóa post
    @DeleteMapping("/{postId}")
    fun deletePost(
        @PathVariable postId: Long,
        @RequestParam userId: Long
    ) = postService.deletePost(postId, userId)
}
