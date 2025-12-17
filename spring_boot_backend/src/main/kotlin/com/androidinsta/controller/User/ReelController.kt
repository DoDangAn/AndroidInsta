package com.androidinsta.controller.User

import com.androidinsta.Model.*
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Service.CloudinaryService
import com.androidinsta.Service.VideoQuality
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller cho reel operations  
 * Handles video reel uploads, viewing, and management
 */
@RestController
@RequestMapping("/api/reels")
class ReelController(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
) {

    /**
     * Upload reel (video post)
     * POST /api/reels/upload
     */
    @PostMapping("/upload")
    @CacheEvict(value = ["reels", "reelDetail", "feedPosts", "userPosts"], allEntries = true)
    fun uploadReel(
        @RequestParam("video") videoFile: MultipartFile,
        @RequestParam("caption", required = false) caption: String?,
        @RequestParam("visibility", defaultValue = "PUBLIC") visibilityStr: String,
        @RequestParam("quality", defaultValue = "HIGH") qualityStr: String
    ): ResponseEntity<ReelUploadResponse> {
        
        if (!videoFile.contentType?.startsWith("video/")!!) {
            throw IllegalArgumentException("File must be a video")
        }

        if (videoFile.size > 200 * 1024 * 1024) {
            throw IllegalArgumentException("Video must be less than 200MB")
        }

        val currentUserId = SecurityUtil.getCurrentUserId()
        val user = userRepository.findById(currentUserId).orElseThrow { IllegalStateException("User not found") }

        val videoQuality = try {
            VideoQuality.valueOf(qualityStr.uppercase())
        } catch (e: Exception) {
            VideoQuality.HIGH
        }
        
        val uploadResult = cloudinaryService.uploadVideo(videoFile, "reels", videoQuality)

        if (uploadResult.duration != null && uploadResult.duration > 900) {
            cloudinaryService.deleteMedia(uploadResult.publicId, true)
            throw IllegalArgumentException("Video must be less than 15 minutes")
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
            mediaFiles = mutableListOf(mediaFile)
        )

        val saved = postRepository.save(post)

        return ResponseEntity.ok(
            ReelUploadResponse(
                success = true,
                message = "Reel uploaded successfully",
                reelId = saved.id,
                videoUrl = uploadResult.url,
                thumbnailUrl = uploadResult.thumbnailUrl,
                duration = uploadResult.duration
            )
        )
    }

    /**
     * Lấy danh sách reels
     * GET /api/reels
     */
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
        
        return ResponseEntity.ok(PageImpl(reels, pageable, posts.totalElements))
    }

    /**
     * Lấy reel by ID
     * GET /api/reels/{id}
     */
    @GetMapping("/{id}")
    fun getReelById(@PathVariable id: Long): ResponseEntity<PostResponse> {
        val post = postRepository.findById(id).orElseThrow { IllegalStateException("Reel not found") }
        
        if (post.mediaFiles.none { it.fileType == MediaType.VIDEO }) {
            throw IllegalArgumentException("This is not a reel")
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

    /**
     * Xóa reel
     * DELETE /api/reels/{id}
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = ["reels", "reelDetail", "feedPosts", "userPosts"], allEntries = true)
    fun deleteReel(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val post = postRepository.findById(id).orElseThrow { IllegalStateException("Reel not found") }

        if (post.user.id != currentUserId) {
            throw IllegalStateException("You don't have permission to delete this reel")
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
