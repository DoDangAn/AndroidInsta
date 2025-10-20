package com.androidinsta.controller

import com.androidinsta.Service.PostService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {
    
    /**
     * GET /api/posts/feed - Lấy feed posts
     * Hiển thị posts của người mình follow + posts public
     */
    @GetMapping("/feed")
    fun getFeed(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<FeedResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val posts = postService.getFeedPosts(userId, pageable)
        
        return ResponseEntity.ok(
            FeedResponse(
                posts = posts.content.map { it.toDto(userId) },
                currentPage = posts.number,
                totalPages = posts.totalPages,
                totalItems = posts.totalElements
            )
        )
    }
    
    /**
     * GET /api/posts/user/{userId} - Lấy posts của một user
     */
    @GetMapping("/user/{userId}")
    fun getUserPosts(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<FeedResponse> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val posts = postService.getUserPosts(userId, currentUserId, pageable)
        
        return ResponseEntity.ok(
            FeedResponse(
                posts = posts.content.map { it.toDto(currentUserId) },
                currentPage = posts.number,
                totalPages = posts.totalPages,
                totalItems = posts.totalElements
            )
        )
    }
    
    /**
     * POST /api/posts - Tạo post mới
     */
    @PostMapping
    fun createPost(@RequestBody request: CreatePostRequest): ResponseEntity<PostDto> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        val visibility = try {
            com.androidinsta.Model.Visibility.valueOf(request.visibility.uppercase())
        } catch (e: Exception) {
            com.androidinsta.Model.Visibility.PUBLIC
        }
        
        val post = postService.createPost(
            userId = userId,
            caption = request.caption,
            visibility = visibility,
            mediaUrls = request.mediaUrls
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(post.toDto(userId))
    }
    
    /**
     * DELETE /api/posts/{postId} - Xóa post
     */
    @DeleteMapping("/{postId}")
    fun deletePost(@PathVariable postId: Long): ResponseEntity<Unit> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        postService.deletePost(postId, userId)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * GET /api/posts/{postId} - Lấy chi tiết một post
     */
    @GetMapping("/{postId}")
    fun getPost(@PathVariable postId: Long): ResponseEntity<PostDto> {
        // TODO: Implement get post by ID with permission check
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }
    
    /**
     * GET /api/posts/advertise - Lấy quảng cáo posts
     * Không cần authentication, ai cũng xem được
     */
    @GetMapping("/advertise")
    fun getAdvertisePosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<FeedResponse> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val posts = postService.getAdvertisePosts(pageable)
        
        return ResponseEntity.ok(
            FeedResponse(
                posts = posts.content.map { it.toDto(currentUserId) },
                currentPage = posts.number,
                totalPages = posts.totalPages,
                totalItems = posts.totalElements
            )
        )
    }
}
