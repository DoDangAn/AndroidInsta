package com.androidinsta.controller.User

import com.androidinsta.Service.PostService
import com.androidinsta.Service.LikeService
import com.androidinsta.Service.CloudinaryService
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
    private val likeService: LikeService,
    private val cloudinaryService: CloudinaryService
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
     * Cache: Được xử lý bởi PostService.getFeedResponse() (Service layer)
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
        if (!SecurityUtil.isAuthenticated()) {
            throw IllegalStateException("User not authenticated")
        }

        val userId = SecurityUtil.getCurrentUserIdOrNull() ?: throw IllegalStateException("User not authenticated")
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val response = postService.getFeedResponse(userId, pageable)
        return ResponseEntity.ok(response)
    }
    
    /**
     * GET /api/posts/user/{userId} - Get user's posts
     *
     * Cache: Được xử lý bởi PostService.getUserPostsResponse() (Service layer)
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
     *
     * Cache: Được xử lý bởi PostService.getPostById() (Service layer)
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
     *
     * Cache: Được xử lý bởi PostService.getAdvertisePostsResponse() (Service layer)
     */
    @GetMapping("/advertise")
    fun getAdvertisePosts(
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "size", required = false, defaultValue = "10") size: Int
    ): ResponseEntity<FeedResponse> {
        val pageable = PageRequest.of(page, size)
        val currentUserId = SecurityUtil.getCurrentUserId()
        val response = postService.getAdvertisePostsResponse(pageable, currentUserId)

        return ResponseEntity.ok(response)
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
        @RequestParam("caption", required = false) caption: String?,
        @RequestParam("visibility", required = false) visibilityStr: String?,
        @RequestParam("mediaUrls", required = false) mediaUrls: List<String>?,
        @RequestParam("images", required = false) images: Array<org.springframework.web.multipart.MultipartFile>?
    ): ResponseEntity<PostDto> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        // Resolve visibility param
        val visibility = try {
            visibilityStr?.let { Visibility.valueOf(it) }
        } catch (e: Exception) {
            null
        }

        // Build final mediaUrls: upload images first if provided, then append mediaUrls
        val finalMediaUrls = mutableListOf<String>()

        if (images != null && images.isNotEmpty()) {
            if (images.size > 10) throw IllegalArgumentException("Maximum 10 images per post")
            images.forEach { file ->
                if (!file.contentType?.startsWith("image/")!!) {
                    throw IllegalArgumentException("All files must be images")
                }
            }
            images.forEach { file ->
                val uploadResult = cloudinaryService.uploadImage(file, "posts", com.androidinsta.Service.ImageQuality.HIGH)
                finalMediaUrls.add(uploadResult.url)
            }
        }

        if (mediaUrls != null && mediaUrls.isNotEmpty()) {
            finalMediaUrls.addAll(mediaUrls)
        }

        val updatedPost = postService.updatePost(
            postId = postId,
            userId = userId,
            caption = caption,
            visibility = visibility,
            mediaUrls = if (finalMediaUrls.isEmpty()) null else finalMediaUrls
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
