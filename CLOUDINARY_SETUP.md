# Cloudinary Configuration - AndroidInsta

## ✅ Cấu hình đã hoàn tất

### Thông tin Cloudinary Account
- **Cloud Name:** `da8ldqctz`
- **API Key:** `456122717799773`
- **API Secret:** `maNikNVr-dxsRWMsjH7VKn1z1L4`

### Cấu hình trong application.properties
```properties
cloudinary.cloud-name=da8ldqctz
cloudinary.api-key=456122717799773
cloudinary.api-secret=maNikNVr-dxsRWMsjH7VKn1z1L4
```

---

## 📂 Folder Structure trên Cloudinary

Tất cả media sẽ được lưu trữ theo cấu trúc:

```
da8ldqctz/
├── posts/          # Ảnh posts thường (carousel, single image)
├── reels/          # Video reels/stories
└── avatars/        # Ảnh đại diện users
```

---

## 🎯 Các Chức Năng Upload

### 1. Upload Post với Multiple Images (Carousel)
**Endpoint:** `POST /api/posts/upload`

**Parameters:**
- `images[]`: Array of image files (max 10)
- `caption`: Caption for post (optional)
- `visibility`: PUBLIC, PRIVATE, ADVERTISE (default: PUBLIC)
- `quality`: HIGH, MEDIUM, LOW (default: HIGH)

**Example:**
```bash
curl -X POST http://localhost:8081/api/posts/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "images=@photo1.jpg" \
  -F "images=@photo2.jpg" \
  -F "caption=Beautiful day!" \
  -F "quality=HIGH"
```

**Tối ưu hóa:**
- ✅ Auto format (WebP cho browser modern, JPEG fallback)
- ✅ Smart compression (giữ nguyên chi tiết)
- ✅ Responsive images (3 versions: high/medium/mobile)
- ✅ Progressive loading
- ✅ Face detection cho smart crop
- ✅ Perceptual hash để detect duplicate

---

### 2. Upload Single Image
**Endpoint:** `POST /api/posts/upload-single`

**Parameters:**
- `image`: Single image file
- `caption`: Optional
- `visibility`: PUBLIC/PRIVATE/ADVERTISE
- `quality`: HIGH/MEDIUM/LOW

**Response:**
```json
{
  "success": true,
  "message": "Post uploaded successfully",
  "postId": 123,
  "imageUrl": "https://res.cloudinary.com/da8ldqctz/image/upload/...",
  "width": 1920,
  "height": 1080,
  "format": "jpg",
  "size": 245678
}
```

---

### 3. Upload Video Reel
**Endpoint:** `POST /api/reels/upload`

**Parameters:**
- `video`: Video file (max 200MB, max 15 minutes)
- `caption`: Optional
- `visibility`: PUBLIC/PRIVATE/ADVERTISE
- `quality`: HIGH/MEDIUM/LOW

**Quality Settings:**
- **HIGH:** 1080p (1920x1080, 5Mbps bitrate)
- **MEDIUM:** 720p (1280x720, 2.5Mbps bitrate)
- **LOW:** 480p (854x480, 1Mbps bitrate)

**Tối ưu hóa:**
- ✅ H.264 codec (best compatibility)
- ✅ AAC audio codec
- ✅ Adaptive bitrate
- ✅ HLS streaming support
- ✅ Auto thumbnail generation (high quality)
- ✅ Mobile-friendly version (720p)
- ✅ Progressive web streaming

**Example:**
```bash
curl -X POST http://localhost:8081/api/reels/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "video=@myvideo.mp4" \
  -F "caption=Check this out!" \
  -F "quality=HIGH"
```

---

### 4. Upload Avatar
**Endpoint:** `POST /api/posts/upload-avatar`

**Parameters:**
- `avatar`: Image file

**Tối ưu hóa:**
- ✅ Face detection & centering
- ✅ Auto crop to circle (500x500)
- ✅ Thumbnail generation (150x150)
- ✅ Smart sharpening
- ✅ Auto delete old avatar

---

## 📊 Image Quality Levels

### HIGH Quality (Full HD)
- **Resolution:** 1920x1920 max
- **Quality:** auto:best (90-100%)
- **Use case:** Professional photos, portfolio
- **File size:** ~300-800KB per image

### MEDIUM Quality (HD)
- **Resolution:** 1080x1080 max
- **Quality:** auto:good (80-90%)
- **Use case:** Regular posts
- **File size:** ~150-400KB per image

### LOW Quality (Standard)
- **Resolution:** 720x720 max
- **Quality:** auto:eco (70-80%)
- **Use case:** Quick posts, stories
- **File size:** ~80-200KB per image

---

## 📹 Video Quality Levels

### HIGH Quality (1080p)
- **Resolution:** 1920x1080
- **Bitrate:** 5Mbps video, 192kbps audio
- **Use case:** High-quality content
- **File size:** ~37.5MB per minute

### MEDIUM Quality (720p)
- **Resolution:** 1280x720
- **Bitrate:** 2.5Mbps video, 128kbps audio
- **Use case:** Standard reels
- **File size:** ~18.75MB per minute

### LOW Quality (480p)
- **Resolution:** 854x480
- **Bitrate:** 1Mbps video, 96kbps audio
- **Use case:** Quick uploads, slow connection
- **File size:** ~7.5MB per minute

---

## 🔗 URL Structure

### Image URLs
```
https://res.cloudinary.com/da8ldqctz/image/upload/v{version}/posts/{publicId}.{format}
```

### Video URLs
```
https://res.cloudinary.com/da8ldqctz/video/upload/v{version}/reels/{publicId}.mp4
```

### Avatar URLs
```
https://res.cloudinary.com/da8ldqctz/image/upload/v{version}/avatars/{publicId}.{format}
```

---

## 🎨 Dynamic Transformations

CloudinaryService hỗ trợ dynamic URL generation:

### Get Optimized Image
```kotlin
val optimizedUrl = cloudinaryService.getOptimizedImageUrl(
    publicId = "posts/abc123",
    width = 640,
    height = 640,
    crop = "fill"
)
```

### Get Optimized Video
```kotlin
val videoUrl = cloudinaryService.getOptimizedVideoUrl(
    publicId = "reels/xyz789",
    quality = VideoQuality.MEDIUM
)
```

---

## 🗑️ Delete Media

### Delete Image
```kotlin
cloudinaryService.deleteMedia(publicId = "posts/abc123", isVideo = false)
```

### Delete Video
```kotlin
cloudinaryService.deleteMedia(publicId = "reels/xyz789", isVideo = true)
```

---

## 📈 Storage Limits

**Free Plan Cloudinary:**
- ✅ 25 Credits/month
- ✅ 25GB Storage
- ✅ 25GB Bandwidth
- ✅ Unlimited transformations

**Current Usage:** Xem tại [Cloudinary Dashboard](https://console.cloudinary.com/da8ldqctz)

---

## 🔒 Security

1. **API Secret được bảo mật** trong `application.properties`
2. **HTTPS only** - tất cả URLs đều dùng `secure: true`
3. **Upload validation:**
   - File type check (image/*, video/*)
   - File size limits (200MB max)
   - Video duration limit (15 minutes max)

---

## 🚀 Performance Tips

1. **Sử dụng quality phù hợp:**
   - HIGH cho photos quan trọng
   - MEDIUM cho posts thường
   - LOW cho quick uploads

2. **Lazy loading images:**
   - Flutter sẽ load progressive images
   - Thumbnail load trước, full resolution sau

3. **Video optimization:**
   - Upload quality HIGH, deliver quality dựa vào network
   - Sử dụng thumbnail cho video preview

4. **CDN auto-caching:**
   - Cloudinary tự động cache worldwide
   - Delivery siêu nhanh từ edge locations

---

## 📱 Flutter Integration

Trong Flutter app, sử dụng:

```dart
// Load image
CachedNetworkImage(
  imageUrl: post.mediaFiles[0].fileUrl,
  placeholder: (context, url) => CircularProgressIndicator(),
  errorWidget: (context, url, error) => Icon(Icons.error),
)

// Load video
VideoPlayerController.network(post.mediaFiles[0].fileUrl)
```

---

## 🔧 Troubleshooting

### Upload thất bại
1. Check Cloudinary credentials
2. Check file size < 200MB
3. Check file format (image/*, video/*)
4. Check network connection

### Image không load
1. Check URL có `https://res.cloudinary.com/da8ldqctz/`
2. Check publicId có đúng không
3. Check CORS settings (đã enable)

### Video không play
1. Check codec: phải là H.264
2. Check format: mp4
3. Check duration < 15 minutes

---

## 📞 Support

- **Cloudinary Console:** https://console.cloudinary.com/da8ldqctz
- **API Documentation:** https://cloudinary.com/documentation
- **Dashboard Usage:** https://console.cloudinary.com/da8ldqctz/usage

