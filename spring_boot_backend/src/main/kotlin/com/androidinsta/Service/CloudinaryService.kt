package com.androidinsta.Service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

data class MediaUploadResult(
    val url: String,
    val publicId: String,
    val thumbnailUrl: String? = null,
    val duration: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val format: String? = null,
    val size: Long? = null
)

enum class ImageQuality {
    HIGH,      // Full HD - 1920x1920
    MEDIUM,    // HD - 1080x1080
    LOW        // Standard - 720x720
}

enum class VideoQuality {
    HIGH,      // 1080p - Full HD
    MEDIUM,    // 720p - HD
    LOW        // 480p - Standard
}

data class VideoConfig(
    val width: Int,
    val height: Int,
    val videoBitrate: String,
    val audioBitrate: String
)

@Service
class CloudinaryService(private val cloudinary: Cloudinary) {

    /**
     * Upload video với tối ưu hóa chất lượng
     * - Tự động chọn codec tốt nhất (H.264)
     * - Tối ưu bitrate cho quality/size balance
     * - Tạo adaptive streaming cho mobile
     */
    fun uploadVideo(
        file: MultipartFile, 
        folder: String = "reels",
        quality: VideoQuality = VideoQuality.HIGH
    ): MediaUploadResult {
        val (width, height, videoBitrate, audioBitrate) = when (quality) {
            VideoQuality.HIGH -> VideoConfig(1920, 1080, "5m", "192k")
            VideoQuality.MEDIUM -> VideoConfig(1280, 720, "2.5m", "128k")
            VideoQuality.LOW -> VideoConfig(854, 480, "1m", "96k")
        }
        
        val uploadParams = ObjectUtils.asMap(
            "resource_type", "video",
            "folder", folder,
            "format", "mp4",
            
            // Video optimization
            "eager", listOf(
                // Main optimized video
                ObjectUtils.asMap(
                    "width", width,
                    "height", height,
                    "crop", "limit",
                    "video_codec", "h264",        // Best compatibility
                    "audio_codec", "aac",         // Best audio codec
                    "bit_rate", videoBitrate,     // Control video bitrate
                    "audio_frequency", 44100,     // Standard audio frequency
                    "quality", "auto:best",       // Auto optimize quality
                    "fetch_format", "auto"        // Auto select best format
                ),
                // Mobile-friendly version (lower bitrate)
                ObjectUtils.asMap(
                    "width", 720,
                    "height", 720,
                    "crop", "fill",
                    "video_codec", "h264",
                    "bit_rate", "1m",
                    "quality", "auto:good",
                    "transformation", "mobile_friendly"
                )
            ),
            
            // Progressive web streaming
            "eager_async", false,              // Process immediately
            "streaming_profile", "hd",         // Enable HLS streaming
            
            // Additional optimizations
            "colors", true,                    // Color optimization
            "duration", 900.0                  // Max 15 minutes
        )

        val result = cloudinary.uploader().upload(file.bytes, uploadParams)
        
        val videoUrl = result["secure_url"] as String
        val publicId = result["public_id"] as String
        val duration = (result["duration"] as? Number)?.toInt()
        val videoWidth = (result["width"] as? Number)?.toInt()
        val videoHeight = (result["height"] as? Number)?.toInt()
        val format = result["format"] as? String
        val bytes = (result["bytes"] as? Number)?.toLong()
        
        // Generate high-quality thumbnail
        val thumbnail = generateVideoThumbnail(publicId, quality)

        return MediaUploadResult(
            url = videoUrl,
            publicId = publicId,
            thumbnailUrl = thumbnail,
            duration = duration,
            width = videoWidth,
            height = videoHeight,
            format = format,
            size = bytes
        )
    }

    /**
     * Upload image với tối ưu hóa chất lượng cao
     * - Auto format (WebP cho browser hỗ trợ, JPEG fallback)
     * - Smart compression giữ nguyên chi tiết
     * - Responsive images cho different devices
     */
    fun uploadImage(
        file: MultipartFile, 
        folder: String = "posts",
        quality: ImageQuality = ImageQuality.HIGH
    ): MediaUploadResult {
        val (maxWidth, maxHeight) = when (quality) {
            ImageQuality.HIGH -> Pair(1920, 1920)      // Full HD
            ImageQuality.MEDIUM -> Pair(1080, 1080)    // HD
            ImageQuality.LOW -> Pair(720, 720)         // Standard
        }
        
        val uploadParams = ObjectUtils.asMap(
            "resource_type", "image",
            "folder", folder,
            
            // Image optimization
            "eager", listOf(
                // Main high-quality image
                ObjectUtils.asMap(
                    "width", maxWidth,
                    "height", maxHeight,
                    "crop", "limit",              // Don't upscale, only limit size
                    "quality", "auto:best",       // Smart quality (90-100)
                    "fetch_format", "auto",       // Auto WebP/JPEG selection
                    "dpr", "auto",                // Device pixel ratio auto
                    "flags", "progressive"        // Progressive JPEG loading
                ),
                // Thumbnail version
                ObjectUtils.asMap(
                    "width", 360,
                    "height", 360,
                    "crop", "fill",
                    "gravity", "auto",            // Smart cropping
                    "quality", "auto:good",
                    "fetch_format", "auto",
                    "transformation", "thumbnail"
                ),
                // Mobile-optimized version
                ObjectUtils.asMap(
                    "width", 640,
                    "height", 640,
                    "crop", "limit",
                    "quality", "auto:eco",        // Smaller file for mobile
                    "fetch_format", "auto",
                    "transformation", "mobile"
                )
            ),
            
            // Processing options
            "eager_async", false,                     // Process immediately
            "colors", true,                           // Extract dominant colors
            "faces", true,                            // Detect faces for smart crop
            "image_metadata", true,                   // Keep EXIF data
            "phash", true,                            // Perceptual hash for duplicate detection
            
            // Additional enhancements
            "effect", "sharpen:100",                  // Subtle sharpening
            "flags", listOf("progressive", "lossy")   // Progressive + smart compression
        )

        val result = cloudinary.uploader().upload(file.bytes, uploadParams)
        
        val imageUrl = result["secure_url"] as String
        val publicId = result["public_id"] as String
        val width = (result["width"] as? Number)?.toInt()
        val height = (result["height"] as? Number)?.toInt()
        val format = result["format"] as? String
        val bytes = (result["bytes"] as? Number)?.toLong()

        return MediaUploadResult(
            url = imageUrl,
            publicId = publicId,
            width = width,
            height = height,
            format = format,
            size = bytes
        )
    }
    
    /**
     * Upload avatar với tối ưu hóa đặc biệt cho ảnh đại diện
     */
    fun uploadAvatar(file: MultipartFile): MediaUploadResult {
        val uploadParams = ObjectUtils.asMap(
            "resource_type", "image",
            "folder", "avatars",
            
            "eager", listOf(
                // Main avatar (500x500 circle)
                ObjectUtils.asMap(
                    "width", 500,
                    "height", 500,
                    "crop", "fill",
                    "gravity", "faces",           // Focus on face
                    "quality", "auto:best",
                    "fetch_format", "auto",
                    "radius", "max",              // Make it circular
                    "effect", "sharpen:100"
                ),
                // Thumbnail (150x150)
                ObjectUtils.asMap(
                    "width", 150,
                    "height", 150,
                    "crop", "fill",
                    "gravity", "faces",
                    "quality", "auto:good",
                    "radius", "max",
                    "transformation", "avatar_thumb"
                )
            ),
            
            "eager_async", false,
            "faces", true,
            "colors", true
        )

        val result = cloudinary.uploader().upload(file.bytes, uploadParams)
        
        val avatarUrl = result["secure_url"] as String
        val publicId = result["public_id"] as String
        val width = (result["width"] as? Number)?.toInt()
        val height = (result["height"] as? Number)?.toInt()

        return MediaUploadResult(
            url = avatarUrl,
            publicId = publicId,
            width = width,
            height = height
        )
    }

    private fun generateVideoThumbnail(publicId: String, quality: VideoQuality): String {
        val (width, height) = when (quality) {
            VideoQuality.HIGH -> Pair(720, 1280)
            VideoQuality.MEDIUM -> Pair(480, 854)
            VideoQuality.LOW -> Pair(360, 640)
        }
        
        return cloudinary.url()
            .resourceType("video")
            .transformation(
                ObjectUtils.asMap(
                    "width", width,
                    "height", height,
                    "crop", "fill",
                    "gravity", "auto",              // Smart focus
                    "start_offset", "auto",         // Auto select best frame
                    "quality", "auto:best",
                    "fetch_format", "auto",         // WebP/JPEG auto
                    "effect", "sharpen:100",        // Sharp thumbnail
                    "format", "jpg"
                )
            )
            .secure(true)
            .generate("$publicId.jpg")
    }
    
    /**
     * Get optimized URL for delivery với responsive sizing
     */
    fun getOptimizedImageUrl(
        publicId: String,
        width: Int? = null,
        height: Int? = null,
        crop: String = "limit"
    ): String {
        val transformations = mutableMapOf<String, Any>(
            "quality" to "auto:best",
            "fetch_format" to "auto",
            "dpr" to "auto"
        )
        
        width?.let { transformations["width"] = it }
        height?.let { transformations["height"] = it }
        transformations["crop"] = crop
        
        return cloudinary.url()
            .transformation(transformations)
            .secure(true)
            .generate(publicId)
    }
    
    /**
     * Get optimized video URL với adaptive streaming
     */
    fun getOptimizedVideoUrl(
        publicId: String,
        quality: VideoQuality = VideoQuality.MEDIUM
    ): String {
        val (width, bitrate) = when (quality) {
            VideoQuality.HIGH -> Pair(1920, "5m")
            VideoQuality.MEDIUM -> Pair(1280, "2.5m")
            VideoQuality.LOW -> Pair(854, "1m")
        }
        
        return cloudinary.url()
            .resourceType("video")
            .transformation(
                ObjectUtils.asMap(
                    "width", width,
                    "crop", "limit",
                    "video_codec", "h264",
                    "bit_rate", bitrate,
                    "quality", "auto:good",
                    "fetch_format", "auto"
                )
            )
            .secure(true)
            .generate(publicId)
    }

    fun deleteMedia(publicId: String, isVideo: Boolean = false): Boolean {
        return try {
            val resourceType = if (isVideo) "video" else "image"
            val result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", resourceType)
            )
            result["result"] == "ok"
        } catch (e: Exception) {
            false
        }
    }
}
