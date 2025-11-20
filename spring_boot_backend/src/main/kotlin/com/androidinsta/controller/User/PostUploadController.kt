package com.androidinsta.controller.User

import com.androidinsta.Model.*
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Service.CloudinaryService
import com.androidinsta.Service.ImageQuality
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.PostMediaFile
import com.androidinsta.dto.PostResponse
import com.androidinsta.dto.PostUserResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/posts")
class PostUploadController(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
) {

    /**
     * Upload post với multiple images (carousel)
     * Support quality settings: HIGH, MEDIUM, LOW
     */
    @PostMapping("/upload")
    fun uploadPost(
        @RequestParam("images") images: Array<MultipartFile>,
        @RequestParam("caption", required = false) caption: String?,
        @RequestParam("visibility", required = false, defaultValue = "PUBLIC") visibilityStr: String,
        @RequestParam("quality", required = false, defaultValue = "HIGH") qualityStr: String
    ): ResponseEntity<Map<String, Any>> {
        
        if (images.isEmpty()) {
            return ResponseEntity.badRequest().body(
                mapOf("success" to false, "message" to "At least one image is required")
            )
        }

        if (images.size > 10) {
            return ResponseEntity.badRequest().body(
                mapOf("success" to false, "message" to "Maximum 10 images per post")
            )
        }

        // Validate all files are images
        images.forEach { file ->
            if (!file.contentType?.startsWith("image/")!!) {
                return ResponseEntity.badRequest().body(
                    mapOf("success" to false, "message" to "All files must be images")
                )
            }
        }

        val currentUserId = SecurityUtil.getCurrentUserId()
        val user = userRepository.findById(currentUserId).orElseThrow()

        try {
            // Parse quality setting
            val imageQuality = try {
                ImageQuality.valueOf(qualityStr.uppercase())
            } catch (e: Exception) {
                ImageQuality.HIGH
            }

            // Upload all images
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
                mapOf(
                    "success" to true,
                    "message" to "Post uploaded successfully",
                    "post" to PostResponse(
                        id = saved.id,
                        user = PostUserResponse(
                            id = user.id,
                            username = user.username
                        ),
                        caption = saved.caption,
                        visibility = saved.visibility,
                        likeCount = 0,
                        commentCount = 0,
                        isLiked = false,
                        createdAt = saved.createdAt,
                        updatedAt = saved.updatedAt,
                        mediaFiles = mediaFiles.map {
                            PostMediaFile(
                                fileUrl = it.fileUrl,
                                fileType = it.fileType.name,
                                orderIndex = it.orderIndex
                            )
                        }
                    )
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(
                mapOf("success" to false, "message" to "Upload failed: ${e.message}")
            )
        }
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
    ): ResponseEntity<Map<String, Any>> {
        
        if (!image.contentType?.startsWith("image/")!!) {
            return ResponseEntity.badRequest().body(
                mapOf("success" to false, "message" to "File must be an image")
            )
        }

        val currentUserId = SecurityUtil.getCurrentUserId()
        val user = userRepository.findById(currentUserId).orElseThrow()

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
                mapOf(
                    "success" to true,
                    "message" to "Post uploaded successfully",
                    "postId" to saved.id,
                    "imageUrl" to uploadResult.url
                ) + uploadResult.width?.let { mapOf("width" to it) }.orEmpty()
                  + uploadResult.height?.let { mapOf("height" to it) }.orEmpty()
                  + uploadResult.format?.let { mapOf("format" to it) }.orEmpty()
                  + uploadResult.size?.let { mapOf("size" to it) }.orEmpty()
            )
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(
                mapOf("success" to false, "message" to "Upload failed: ${e.message}")
            )
        }
    }

    /**
     * Update user avatar với face detection
     */
    @PostMapping("/upload-avatar")
    fun uploadAvatar(
        @RequestParam("avatar") avatar: MultipartFile
    ): ResponseEntity<Map<String, Any>> {
        
        if (!avatar.contentType?.startsWith("image/")!!) {
            return ResponseEntity.badRequest().body(
                mapOf("success" to false, "message" to "File must be an image")
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
                mapOf(
                    "success" to true,
                    "message" to "Avatar updated successfully",
                    "avatarUrl" to uploadResult.url
                ) + uploadResult.thumbnailUrl?.let { mapOf("thumbnailUrl" to it) }.orEmpty()
            )
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(
                mapOf("success" to false, "message" to "Upload failed: ${e.message}")
            )
        }
    }
}
