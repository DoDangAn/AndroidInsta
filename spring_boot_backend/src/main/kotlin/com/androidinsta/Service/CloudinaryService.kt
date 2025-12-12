package com.androidinsta.Service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaUploadResult(
    @JsonProperty("url") val url: String,
    @JsonProperty("publicId") val publicId: String,
    @JsonProperty("thumbnailUrl") val thumbnailUrl: String? = null,
    @JsonProperty("duration") val duration: Int? = null,
    @JsonProperty("width") val width: Int? = null,
    @JsonProperty("height") val height: Int? = null,
    @JsonProperty("format") val format: String? = null,
    @JsonProperty("size") val size: Long? = null
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class VideoConfig(
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("videoBitrate") val videoBitrate: String,
    @JsonProperty("audioBitrate") val audioBitrate: String
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
            "quality", "auto:best",
            "transformation", "w_${width},h_${height},c_limit,vc_h264,ac_aac,br_${videoBitrate},q_auto:best"
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
            "quality", "auto:best",
            "fetch_format", "auto",
            "colors", true,
            "faces", true,
            "image_metadata", true,
            "phash", true,
            "transformation", "w_${maxWidth},h_${maxHeight},c_limit,q_auto:best,f_auto"
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
            "quality", "auto:best",
            "fetch_format", "auto",
            "faces", true,
            "colors", true,
            "transformation", "w_500,h_500,c_fill,g_faces,q_auto:best,r_max,e_sharpen:100"
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
        
        // Build URL with transformation parameters directly in the URL
        return "https://res.cloudinary.com/${cloudinary.config.cloudName}/video/upload/w_${width},h_${height},c_fill,g_auto,q_auto:best,f_auto,e_sharpen:100/${publicId}.jpg"
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
