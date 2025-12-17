package com.androidinsta.controller.User

import com.androidinsta.Model.*
import com.androidinsta.Service.PostService
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Service.CloudinaryService
import com.androidinsta.Service.ImageQuality
import com.androidinsta.Service.RedisService
import org.springframework.cache.CacheManager
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller cho post upload operations
 * Handles uploading images and videos for posts, including avatars
 */
@RestController
@RequestMapping("/api/posts")
class PostUploadController(
    private val postService: PostService,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService,
    private val redisService: RedisService,
    private val cacheManager: CacheManager
) {

    /**
     * Upload post với multiple images (carousel)
     * POST /api/posts/upload
     */
    @PostMapping("/upload")
    fun uploadPost(
        @RequestParam("images") images: Array<MultipartFile>,
        @RequestParam("caption", required = false) caption: String?,
        @RequestParam("visibility", defaultValue = "PUBLIC") visibilityStr: String,
        @RequestParam("quality", defaultValue = "HIGH") qualityStr: String
    ): ResponseEntity<PostUploadResponse> {
        
        if (images.isEmpty()) {
            throw IllegalArgumentException("At least one image is required")
        }

        if (images.size > 10) {
            throw IllegalArgumentException("Maximum 10 images per post")
        }

        images.forEach { file ->
            if (!file.contentType?.startsWith("image/")!!) {
                throw IllegalArgumentException("All files must be images")
            }
        }

        val currentUserId = SecurityUtil.getCurrentUserId()
        val user = userRepository.findById(currentUserId).orElseThrow { IllegalStateException("User not found") }

        val imageQuality = try {
            ImageQuality.valueOf(qualityStr.uppercase())
        } catch (e: Exception) {
            ImageQuality.HIGH
        }

        val mediaUrls = images.mapIndexed { index, file ->
            val uploadResult = cloudinaryService.uploadImage(file, "posts", imageQuality)
            uploadResult.url
        }

        val saved = postService.createPost(
            userId = currentUserId,
            caption = caption ?: "",
            visibility = Visibility.valueOf(visibilityStr.uppercase()),
            mediaUrls = mediaUrls
        )

        return ResponseEntity.ok(
            PostUploadResponse(
                success = true,
                message = "Post uploaded successfully",
                postId = saved.id,
                imageUrls = mediaUrls
            )
        )
    }

    /**
     * Upload single image post (optimized)
     */
    @PostMapping("/upload-single")
    fun uploadSingleImage(
        @RequestParam("image") image: MultipartFile,
        @RequestParam("caption", required = false) caption: String?,
        @RequestParam("visibility", required = false, defaultValue = "PUBLIC") visibilityStr: String,
        @RequestParam("quality", required = false, defaultValue = "HIGH") qualityStr: String
    ): ResponseEntity<UploadResponse> {
        
        if (!image.contentType?.startsWith("image/")!!) {
            return ResponseEntity.badRequest().body(
                UploadResponse(success = false, message = "File must be an image")
            )
        }

        val currentUserId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                UploadResponse(success = false, message = "User not authenticated")
            )
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            RuntimeException("User not found")
        }

        try {
            val imageQuality = try {
                ImageQuality.valueOf(qualityStr.uppercase())
            } catch (e: Exception) {
                ImageQuality.HIGH
            }

            val uploadResult = cloudinaryService.uploadImage(image, "posts", imageQuality)

            // Persist post via service
            postService.createPost(
                userId = currentUserId,
                caption = caption ?: "",
                visibility = Visibility.valueOf(visibilityStr.uppercase()),
                mediaUrls = listOf(uploadResult.url)
            )

            return ResponseEntity.ok(
                UploadResponse(
                    success = true,
                    message = "Post uploaded successfully",
                    url = uploadResult.url,
                    publicId = uploadResult.publicId,
                    width = uploadResult.width,
                    height = uploadResult.height,
                    format = uploadResult.format,
                    size = uploadResult.size
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(
                UploadResponse(success = false, message = "Upload failed: ${e.message}")
            )
        }
    }

    /**
     * Update user avatar với face detection
     */
    @PostMapping("/upload-avatar")
    fun uploadAvatar(
        @RequestParam("avatar") avatar: MultipartFile
    ): ResponseEntity<UploadResponse> {
        if (!avatar.contentType?.startsWith("image/")!!) {
            return ResponseEntity.badRequest().body(
                UploadResponse(success = false, message = "File must be an image")
            )
        }

        val currentUserId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                UploadResponse(success = false, message = "User not authenticated")
            )

        val user = userRepository.findById(currentUserId).orElseThrow { RuntimeException("User not found") }

        try {
            // Attempt to delete old avatar if exists, but don't fail the whole request on delete error
            user.avatarUrl?.let { oldUrl ->
                try {
                    val publicIdMatch = Regex("avatars/[^/]+").find(oldUrl)
                    publicIdMatch?.value?.let { cloudinaryService.deleteMedia(it, false) }
                } catch (e: Exception) {
                    println("Warning: failed to delete old avatar for user $currentUserId: ${e.message}")
                }
            }

            val uploadResult = cloudinaryService.uploadAvatar(avatar)

            // Update user avatar
            val updatedUser = user.copy(avatarUrl = uploadResult.url)
            userRepository.save(updatedUser)

            // Evict caches and related redis keys so new avatar propagates quickly
            try {
                cacheManager.getCache("feedPosts")?.clear()
                cacheManager.getCache("userPosts")?.clear()
            } catch (e: Exception) {
                println("Warning: failed to evict caches after avatar update for user $currentUserId: ${e.message}")
            }

            // Invalidate simple redis keys related to the user profile
            try {
                redisService.delete("user:${currentUserId}:stats")
                redisService.delete("user:${currentUserId}:profile")
            } catch (e: Exception) {
                println("Warning: failed to evict redis keys after avatar update for user $currentUserId: ${e.message}")
            }

            return ResponseEntity.ok(
                UploadResponse(
                    success = true,
                    message = "Avatar updated successfully",
                    url = uploadResult.url,
                    thumbnailUrl = uploadResult.thumbnailUrl
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(
                UploadResponse(success = false, message = "Upload failed: ${e.message}")
            )
        }
    }
}
