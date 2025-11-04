package com.androidinsta.controller.User

import com.androidinsta.Model.*
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Service.CloudinaryService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.PostMediaFile
import com.androidinsta.dto.PostResponse
import com.androidinsta.dto.PostUserResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/reels")
class ReelController(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
) {

    @PostMapping("/upload")
    fun uploadReel(
        @RequestParam("video") videoFile: MultipartFile,
        @RequestParam("caption", required = false) caption: String?,
        @RequestParam("visibility", required = false, defaultValue = "PUBLIC") visibilityStr: String,
        @RequestParam("quality", required = false, defaultValue = "HIGH") qualityStr: String
    ): ResponseEntity<Map<String, Any>> {
        
        if (!videoFile.contentType?.startsWith("video/")!!) {
            return ResponseEntity.badRequest().body(
                mapOf("success" to false, "message" to "File must be a video")
            )
        }

        if (videoFile.size > 200 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(
                mapOf("success" to false, "message" to "Video must be less than 200MB")
            )
        }

        val currentUserId = SecurityUtil.getCurrentUserId()
        val user = userRepository.findById(currentUserId).orElseThrow()

        try {
            // Parse quality setting
            val videoQuality = try {
                com.androidinsta.Service.VideoQuality.valueOf(qualityStr.uppercase())
            } catch (e: Exception) {
                com.androidinsta.Service.VideoQuality.HIGH
            }
            
            val uploadResult = cloudinaryService.uploadVideo(videoFile, "reels", videoQuality)

            if (uploadResult.duration != null && uploadResult.duration > 900) {
                cloudinaryService.deleteMedia(uploadResult.publicId, true)
                return ResponseEntity.badRequest().body(
                    mapOf("success" to false, "message" to "Video must be less than 15 minutes")
                )
            }

            val mediaFile = MediaFile(
                fileUrl = uploadResult.url,
                fileType = MediaType.VIDEO,
                orderIndex = 1,
                cloudinaryPublicId = uploadResult.publicId,
                duration = uploadResult.duration,
                thumbnailUrl = uploadResult.thumbnailUrl
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
                    "message" to "Reel uploaded successfully",
                    "post" to PostResponse(
                        id = saved.id,
                        user = PostUserResponse(user.id, user.username),
                        caption = saved.caption,
                        visibility = saved.visibility,
                        mediaFiles = listOf(
                            PostMediaFile(
                                mediaFile.fileUrl,
                                mediaFile.fileType.name,
                                mediaFile.orderIndex,
                                mediaFile.duration,
                                mediaFile.thumbnailUrl
                            )
                        ),
                        createdAt = saved.createdAt,
                        updatedAt = saved.updatedAt
                    )
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(
                mapOf("success" to false, "message" to "Upload failed: ${e.message}")
            )
        }
    }

    @GetMapping
    fun getReels(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val posts = postRepository.findAll(pageable)
        
        val reels = posts.content
            .filter { it.mediaFiles.any { m -> m.fileType == MediaType.VIDEO } }
            .map { post ->
                PostResponse(
                    id = post.id,
                    user = PostUserResponse(post.user.id, post.user.username),
                    caption = post.caption,
                    visibility = post.visibility,
                    mediaFiles = post.mediaFiles.map { m ->
                        PostMediaFile(m.fileUrl, m.fileType.name, m.orderIndex, m.duration, m.thumbnailUrl)
                    },
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt
                )
            }
        
        return ResponseEntity.ok(
            org.springframework.data.domain.PageImpl(reels, pageable, posts.totalElements)
        )
    }

    @GetMapping("/{id}")
    fun getReelById(@PathVariable id: Long): ResponseEntity<PostResponse> {
        val post = postRepository.findById(id).orElseThrow()
        
        if (post.mediaFiles.none { it.fileType == MediaType.VIDEO }) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(
            PostResponse(
                id = post.id,
                user = PostUserResponse(post.user.id, post.user.username),
                caption = post.caption,
                visibility = post.visibility,
                mediaFiles = post.mediaFiles.map { m ->
                    PostMediaFile(m.fileUrl, m.fileType.name, m.orderIndex, m.duration, m.thumbnailUrl)
                },
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteReel(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val post = postRepository.findById(id).orElseThrow()

        if (post.user.id != currentUserId) {
            return ResponseEntity.status(403).body(mapOf("message" to "Forbidden"))
        }

        post.mediaFiles.forEach { media ->
            if (media.fileType == MediaType.VIDEO && media.cloudinaryPublicId != null) {
                cloudinaryService.deleteMedia(media.cloudinaryPublicId, true)
            }
        }

        postRepository.delete(post)
        return ResponseEntity.ok(mapOf("message" to "Reel deleted"))
    }
}
