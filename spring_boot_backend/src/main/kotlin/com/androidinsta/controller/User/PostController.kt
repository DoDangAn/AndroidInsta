package com.androidinsta.controller.User

import com.androidinsta.Service.PostService
import com.androidinsta.Service.LikeService
import com.androidinsta.Model.Visibility
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Post Controller
 * Handles CRUD operations for posts, feed, and interactions
 */
@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService,
    private val likeService: LikeService
) {
    
    /**
     * GET /api/posts/feed - Get personalized feed (Instagram-like behavior)
     * 
     * Feed hiển thị:
     * 1. Posts từ những người user đã follow
     * 2. Posts của chính user
     * 3. ADVERTISE posts (quảng cáo)
     * 
     * KHÔNG hiển thị random public posts từ người lạ
     * 
     * Cached at Service layer với key: userId_page_size
     * Cache sẽ bị invalidate khi có post mới được tạo
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return FeedResponse chứa danh sách posts được phân trang
     */
    @GetMapping("/feed")
    fun getFeed(
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "size", required = false, defaultValue = "20") size: Int
    ): ResponseEntity<FeedResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val response = postService.getFeedResponse(userId, pageable)
        return ResponseEntity.ok(response)
    }
    
    /**
     * GET /api/posts/user/{userId} - Get user's posts
     */
    @GetMapping("/user/{userId}")
    fun getUserPosts(
        @PathVariable userId: Long,
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "size", required = false, defaultValue = "20") size: Int
    ): ResponseEntity<FeedResponse> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val response = postService.getUserPostsResponse(userId, currentUserId, pageable)
        return ResponseEntity.ok(response)
    }
    
    /**
     * GET /api/posts/{postId} - Get post details
     */
    @GetMapping("/{postId}")
    fun getPost(@PathVariable postId: Long): ResponseEntity<PostDto> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val post = postService.getPostById(postId, currentUserId)
        return ResponseEntity.ok(post.toDto(currentUserId))
    }
    
    /**
     * GET /api/posts/advertise - Get recent PUBLIC posts (last 7 days)
     * Hiển thị các bài viết PUBLIC được đăng trong vòng 1 tuần gần đây
     */
    @GetMapping("/advertise")
    fun getAdvertisePosts(
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "size", required = false, defaultValue = "10") size: Int
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
    
    /**
     * POST /api/posts - Create new post
     * 
     * Request Body Validation:
     * - caption: Optional, max 2200 characters
     * - visibility: Required, must be "PUBLIC", "PRIVATE", or "ADVERTISE"
     * - mediaUrls: Required, min 1 max 10 URLs, must be valid Cloudinary URLs
     * 
     * Response:
     * - 201 CREATED: Post được tạo thành công
     * - 400 BAD REQUEST: Validation errors
     * - 401 UNAUTHORIZED: User chưa authenticate
     * 
     * Cache Eviction:
     * - Invalidate tất cả feed, user posts, advertise posts cache
     * - Đảm bảo post mới hiển thị ngay lập tức
     * 
     * @param request CreatePostRequest DTO đã validated
     * @return PostDto với HTTP 201 status
     */
    @PostMapping
    @org.springframework.cache.annotation.CacheEvict(
        value = ["feedPosts", "userPosts", "advertisePosts", "postDetail"], 
        allEntries = true
    )
    fun createPost(@Valid @RequestBody request: CreatePostRequest): ResponseEntity<PostDto> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val post = postService.createPost(
            userId = userId,
            caption = request.caption,
            visibility = Visibility.valueOf(request.visibility),
            mediaUrls = request.mediaUrls
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(post.toDto(userId))
    }
    
    /**
     * PUT /api/posts/{postId} - Update post
     */
    @PutMapping("/{postId}")
    @org.springframework.cache.annotation.CacheEvict(
        value = ["feedPosts", "userPosts", "advertisePosts", "postDetail"], 
        allEntries = true
    )
    fun updatePost(
        @PathVariable postId: Long,
        @Valid @RequestBody request: PostUpdateRequest
    ): ResponseEntity<PostDto> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
            
        val updatedPost = postService.updatePost(
            postId = postId,
            userId = userId,
            caption = request.caption,
            visibility = request.visibility
        )
        
        return ResponseEntity.ok(updatedPost.toDto(userId))
    }
    
    /**
     * DELETE /api/posts/{postId} - Delete post
     */
    @DeleteMapping("/{postId}")
    @org.springframework.cache.annotation.CacheEvict(
        value = ["feedPosts", "userPosts", "advertisePosts", "postDetail"], 
        allEntries = true
    )
    fun deletePost(@PathVariable postId: Long): ResponseEntity<Void> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        postService.deletePost(postId, userId)
        return ResponseEntity.noContent().build()
    }

    /**
     * POST /api/posts/{postId}/like - Like a post
     * 
     * Business Logic:
     * - Idempotent: Nếu đã like rồi thì không tạo duplicate
     * - Tạo Like entity với composite key (userId, postId)
     * - Tăng like count của post
     * - Có thể trigger notification cho chủ post
     * 
     * Response:
     * - success: true nếu like thành công, false nếu đã like trước đó
     * - likesCount: Tổng số likes hiện tại của post
     * 
     * Cache Eviction:
     * - Invalidate feed và user posts cache vì like count thay đổi
     * 
     * @param postId ID của post cần like
     * @return LikeResponse với status và like count
     */
    @PostMapping("/{postId}/like")
    @org.springframework.cache.annotation.CacheEvict(
        value = ["feedPosts", "userPosts"], 
        allEntries = true
    )
    fun likePost(@PathVariable postId: Long): ResponseEntity<LikeResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val liked = likeService.likePost(userId, postId)
        val likesCount = likeService.getLikeCount(postId)
        
        return ResponseEntity.ok(
            LikeResponse(
                success = liked,
                message = if (liked) "Post liked successfully" else "Post already liked",
                likesCount = likesCount
            )
        )
    }

    /**
     * DELETE /api/posts/{postId}/like - Unlike a post
     */
    @DeleteMapping("/{postId}/like")
    @org.springframework.cache.annotation.CacheEvict(
        value = ["feedPosts", "userPosts"], 
        allEntries = true
    )
    fun unlikePost(@PathVariable postId: Long): ResponseEntity<LikeResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val unliked = likeService.unlikePost(userId, postId)
        val likesCount = likeService.getLikeCount(postId)
        
        return ResponseEntity.ok(
            LikeResponse(
                success = unliked,
                message = if (unliked) "Post unliked successfully" else "Post was not liked",
                likesCount = likesCount
            )
        )
    }
    
    /**
     * GET /api/posts/{postId}/like/count - Get like count
     */
    @GetMapping("/{postId}/like/count")
    fun getLikeCount(@PathVariable postId: Long): ResponseEntity<CountResponse> {
        val count = likeService.getLikeCount(postId)
        return ResponseEntity.ok(
            CountResponse(
                success = true,
                count = count,
                message = "Like count retrieved successfully"
            )
        )
    }
    
    /**
     * GET /api/posts/{postId}/like/status - Check if current user liked the post
     */
    @GetMapping("/{postId}/like/status")
    fun getLikeStatus(@PathVariable postId: Long): ResponseEntity<LikeStatusResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val isLiked = likeService.isPostLikedByUser(postId, userId)
        
        return ResponseEntity.ok(
            LikeStatusResponse(
                success = true,
                isLiked = isLiked
            )
        )
    }
    
    // Comment endpoints have been moved to CommentController
}
