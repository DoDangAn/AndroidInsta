package com.androidinsta.controller.User

import com.androidinsta.Model.*
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Service.CloudinaryService
import com.androidinsta.Service.ImageQuality
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
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
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

        val mediaFiles = images.mapIndexed { index, file ->
            val uploadResult = cloudinaryService.uploadImage(file, "posts", imageQuality)
            
            MediaFile(
                fileUrl = uploadResult.url,
                fileType = MediaType.IMAGE,
                orderIndex = index + 1,
                cloudinaryPublicId = uploadResult.publicId
            )
        }

        val post = Post(
            user = user,
            caption = caption,
            visibility = Visibility.valueOf(visibilityStr.uppercase()),
            mediaFiles = mediaFiles
        )

        val saved = postRepository.save(post)

        return ResponseEntity.ok(
            PostUploadResponse(
                success = true,
                message = "Post uploaded successfully",
                postId = saved.id,
                imageUrls = mediaFiles.map { it.fileUrl }
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

            val mediaFile = MediaFile(
                fileUrl = uploadResult.url,
                fileType = MediaType.IMAGE,
                orderIndex = 1,
                cloudinaryPublicId = uploadResult.publicId
            )

            val post = Post(
                user = user,
                caption = caption,
                visibility = Visibility.valueOf(visibilityStr.uppercase()),
                mediaFiles = listOf(mediaFile)
            )

            val saved = postRepository.save(post)

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
        val user = userRepository.findById(currentUserId).orElseThrow()

        try {
            // Delete old avatar if exists
            user.avatarUrl?.let { oldUrl ->
                // Extract publicId from URL and delete
                val publicIdMatch = Regex("avatars/[^/]+").find(oldUrl)
                publicIdMatch?.value?.let { cloudinaryService.deleteMedia(it, false) }
            }

            val uploadResult = cloudinaryService.uploadAvatar(avatar)

            // Update user avatar
            val updatedUser = user.copy(avatarUrl = uploadResult.url)
            userRepository.save(updatedUser)

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
