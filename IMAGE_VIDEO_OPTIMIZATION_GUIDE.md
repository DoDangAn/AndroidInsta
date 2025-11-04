# Image & Video Optimization API - AndroidInsta

## Tổng quan
Hệ thống tối ưu hóa ảnh và video với Cloudinary để đảm bảo chất lượng tốt nhất, tốc độ load nhanh, và tiết kiệm băng thông.

## Tính năng Tối ưu hóa

### 🎬 **Video Optimization**

#### Features:
- ✅ **Smart Codec:** H.264 (best compatibility)
- ✅ **Audio Optimization:** AAC codec, 44.1kHz
- ✅ **Bitrate Control:** Tự động điều chỉnh theo quality
- ✅ **Adaptive Streaming:** HLS support cho smooth playback
- ✅ **Multiple Versions:** Main + Mobile-friendly version
- ✅ **Smart Thumbnail:** Auto-select best frame
- ✅ **Progressive Loading:** Load dần cho mobile

#### Quality Levels:
```kotlin
enum class VideoQuality {
    HIGH,      // 1080p - 1920x1080, bitrate 5mbps
    MEDIUM,    // 720p - 1280x720, bitrate 2.5mbps
    LOW        // 480p - 854x480, bitrate 1mbps
}
```

### 📷 **Image Optimization**

#### Features:
- ✅ **Auto Format:** WebP cho browser hỗ trợ, JPEG fallback
- ✅ **Smart Compression:** Giữ nguyên chi tiết quan trọng
- ✅ **Progressive JPEG:** Load dần từ blur → sharp
- ✅ **Responsive Versions:** 3 versions (main, mobile, thumbnail)
- ✅ **Face Detection:** Smart crop focus vào khuôn mặt
- ✅ **Sharpening:** Subtle enhancement
- ✅ **DPR Auto:** Tự động cho retina displays

#### Quality Levels:
```kotlin
enum class ImageQuality {
    HIGH,      // Full HD - 1920x1920
    MEDIUM,    // HD - 1080x1080
    LOW        // Standard - 720x720
}
```

### 👤 **Avatar Optimization**

#### Features:
- ✅ **Face-Focused Crop:** Auto detect và center vào mặt
- ✅ **Circular Format:** Radius max cho avatar tròn
- ✅ **2 Versions:** Main (500x500) + Thumbnail (150x150)
- ✅ **Sharp Quality:** Extra sharpening cho clarity

## API Endpoints

### 1. Upload Post với Multiple Images (Carousel)
```http
POST /api/posts/upload
Authorization: Bearer {access_token}
Content-Type: multipart/form-data

Form Data:
- images: File[] (max 10 images)
- caption: String (optional)
- visibility: "PUBLIC" | "PRIVATE" (default: PUBLIC)
- quality: "HIGH" | "MEDIUM" | "LOW" (default: HIGH)
```

**Example with cURL:**
```bash
curl -X POST http://localhost:8081/api/posts/upload \
  -H "Authorization: Bearer {token}" \
  -F "images=@photo1.jpg" \
  -F "images=@photo2.jpg" \
  -F "images=@photo3.jpg" \
  -F "caption=Beautiful day!" \
  -F "quality=HIGH"
```

**Response:**
```json
{
  "success": true,
  "message": "Post uploaded successfully",
  "post": {
    "id": 123,
    "userId": 1,
    "caption": "Beautiful day!",
    "visibility": "PUBLIC",
    "likesCount": 0,
    "commentsCount": 0,
    "mediaFiles": [
      {
        "fileUrl": "https://res.cloudinary.com/.../photo1.jpg",
        "fileType": "IMAGE",
        "orderIndex": 1
      },
      {
        "fileUrl": "https://res.cloudinary.com/.../photo2.jpg",
        "fileType": "IMAGE",
        "orderIndex": 2
      }
    ],
    "createdAt": "2025-11-04T10:30:00"
  }
}
```

### 2. Upload Single Image Post
```http
POST /api/posts/upload-single
Authorization: Bearer {access_token}
Content-Type: multipart/form-data

Form Data:
- image: File
- caption: String (optional)
- visibility: "PUBLIC" | "PRIVATE" (default: PUBLIC)
- quality: "HIGH" | "MEDIUM" | "LOW" (default: HIGH)
```

**Response:**
```json
{
  "success": true,
  "message": "Post uploaded successfully",
  "postId": 123,
  "imageUrl": "https://res.cloudinary.com/.../image.jpg",
  "width": 1920,
  "height": 1080,
  "format": "jpg",
  "size": 245678
}
```

### 3. Upload Reel (Video)
```http
POST /api/reels/upload
Authorization: Bearer {access_token}
Content-Type: multipart/form-data

Form Data:
- video: File (max 200MB, max 15 minutes)
- caption: String (optional)
- visibility: "PUBLIC" | "PRIVATE" (default: PUBLIC)
- quality: "HIGH" | "MEDIUM" | "LOW" (default: HIGH)
```

**Example:**
```bash
curl -X POST http://localhost:8081/api/reels/upload \
  -H "Authorization: Bearer {token}" \
  -F "video=@myvideo.mp4" \
  -F "caption=Check this out!" \
  -F "quality=HIGH"
```

**Response:**
```json
{
  "success": true,
  "message": "Reel uploaded successfully",
  "post": {
    "id": 456,
    "mediaFiles": [
      {
        "fileUrl": "https://res.cloudinary.com/.../video.mp4",
        "fileType": "VIDEO",
        "duration": 30,
        "thumbnailUrl": "https://res.cloudinary.com/.../thumb.jpg"
      }
    ]
  }
}
```

### 4. Upload Avatar
```http
POST /api/posts/upload-avatar
Authorization: Bearer {access_token}
Content-Type: multipart/form-data

Form Data:
- avatar: File
```

**Response:**
```json
{
  "success": true,
  "message": "Avatar updated successfully",
  "avatarUrl": "https://res.cloudinary.com/avatars/avatar_500x500.jpg",
  "thumbnailUrl": "https://res.cloudinary.com/avatars/avatar_150x150.jpg"
}
```

## Optimization Details

### Video Processing

#### HIGH Quality (1080p):
```kotlin
- Resolution: 1920x1080
- Video Bitrate: 5 Mbps
- Audio Bitrate: 192 kbps
- Codec: H.264
- Audio: AAC 44.1kHz
- Best for: WiFi connections, high-quality displays
```

#### MEDIUM Quality (720p):
```kotlin
- Resolution: 1280x720
- Video Bitrate: 2.5 Mbps
- Audio Bitrate: 128 kbps
- Best for: Mobile data, standard displays
```

#### LOW Quality (480p):
```kotlin
- Resolution: 854x480
- Video Bitrate: 1 Mbps
- Audio Bitrate: 96 kbps
- Best for: Slow connections, data saving
```

### Image Processing

#### HIGH Quality:
```kotlin
- Max Size: 1920x1920
- Quality: auto:best (90-100%)
- Format: Auto (WebP/JPEG)
- Progressive: Yes
- Sharpening: +100
- Versions:
  - Main: 1920x1920 (full quality)
  - Mobile: 640x640 (eco quality)
  - Thumbnail: 360x360 (good quality)
```

#### MEDIUM Quality:
```kotlin
- Max Size: 1080x1080
- Quality: auto:good (75-90%)
- Same optimizations as HIGH
```

#### LOW Quality:
```kotlin
- Max Size: 720x720
- Quality: auto:eco (60-75%)
- Same optimizations as HIGH
```

### Avatar Processing:
```kotlin
- Main: 500x500 circular
- Thumbnail: 150x150 circular
- Crop: Face-focused (gravity: faces)
- Quality: auto:best
- Format: Auto (WebP/JPEG)
- Sharpening: +100
```

## Cloudinary Transformations

### URL Structure:
```
https://res.cloudinary.com/{cloud_name}/{resource_type}/{transformations}/{version}/{public_id}.{format}
```

### Common Transformations:

#### Responsive Image:
```
/w_1080,h_1080,c_limit,q_auto:best,f_auto,dpr_auto/image.jpg
```

#### Video with Adaptive Streaming:
```
/w_1920,c_limit,vc_h264,br_5m,q_auto:good,f_auto/video.mp4
```

#### Circular Avatar:
```
/w_500,h_500,c_fill,g_faces,r_max,e_sharpen:100,q_auto:best,f_auto/avatar.jpg
```

#### Smart Thumbnail from Video:
```
/w_720,h_1280,c_fill,g_auto,so_auto,q_auto:best,f_auto/video.jpg
```

## Flutter Integration

### 1. Upload Single Image
```dart
import 'package:http/http.dart' as http;
import 'package:image_picker/image_picker.dart';
import 'dart:io';

class PostService {
  final String baseUrl = 'http://10.0.2.2:8081/api/posts';

  Future<Map<String, dynamic>> uploadSingleImage({
    required String accessToken,
    required File imageFile,
    String? caption,
    String visibility = 'PUBLIC',
    String quality = 'HIGH',
  }) async {
    var request = http.MultipartRequest(
      'POST',
      Uri.parse('$baseUrl/upload-single'),
    );

    request.headers['Authorization'] = 'Bearer $accessToken';
    request.fields['caption'] = caption ?? '';
    request.fields['visibility'] = visibility;
    request.fields['quality'] = quality;

    request.files.add(
      await http.MultipartFile.fromPath('image', imageFile.path),
    );

    var response = await request.send();
    var responseData = await response.stream.bytesToString();
    
    if (response.statusCode == 200) {
      return jsonDecode(responseData);
    }
    throw Exception('Failed to upload image');
  }

  // Pick and upload image
  Future<void> pickAndUploadImage(String token) async {
    final picker = ImagePicker();
    final image = await picker.pickImage(
      source: ImageSource.gallery,
      maxWidth: 1920,
      maxHeight: 1920,
      imageQuality: 85,
    );

    if (image != null) {
      final result = await uploadSingleImage(
        accessToken: token,
        imageFile: File(image.path),
        caption: 'My new post!',
        quality: 'HIGH',
      );
      print('Upload success: ${result['imageUrl']}');
    }
  }
}
```

### 2. Upload Multiple Images (Carousel)
```dart
Future<Map<String, dynamic>> uploadMultipleImages({
  required String accessToken,
  required List<File> imageFiles,
  String? caption,
  String visibility = 'PUBLIC',
  String quality = 'HIGH',
}) async {
  if (imageFiles.isEmpty || imageFiles.length > 10) {
    throw Exception('Must upload 1-10 images');
  }

  var request = http.MultipartRequest(
    'POST',
    Uri.parse('$baseUrl/upload'),
  );

  request.headers['Authorization'] = 'Bearer $accessToken';
  request.fields['caption'] = caption ?? '';
  request.fields['visibility'] = visibility;
  request.fields['quality'] = quality;

  // Add all images
  for (var imageFile in imageFiles) {
    request.files.add(
      await http.MultipartFile.fromPath('images', imageFile.path),
    );
  }

  var response = await request.send();
  var responseData = await response.stream.bytesToString();
  
  if (response.statusCode == 200) {
    return jsonDecode(responseData);
  }
  throw Exception('Failed to upload images');
}
```

### 3. Upload Video (Reel)
```dart
Future<Map<String, dynamic>> uploadReel({
  required String accessToken,
  required File videoFile,
  String? caption,
  String visibility = 'PUBLIC',
  String quality = 'HIGH',
}) async {
  var request = http.MultipartRequest(
    'POST',
    Uri.parse('http://10.0.2.2:8081/api/reels/upload'),
  );

  request.headers['Authorization'] = 'Bearer $accessToken';
  request.fields['caption'] = caption ?? '';
  request.fields['visibility'] = visibility;
  request.fields['quality'] = quality;

  request.files.add(
    await http.MultipartFile.fromPath('video', videoFile.path),
  );

  var response = await request.send();
  var responseData = await response.stream.bytesToString();
  
  if (response.statusCode == 200) {
    return jsonDecode(responseData);
  }
  throw Exception('Failed to upload video');
}

// Pick and upload video
Future<void> pickAndUploadVideo(String token) async {
  final picker = ImagePicker();
  final video = await picker.pickVideo(
    source: ImageSource.gallery,
    maxDuration: Duration(minutes: 15),
  );

  if (video != null) {
    // Show quality selection dialog
    final quality = await showQualityDialog(); // HIGH, MEDIUM, LOW
    
    final result = await uploadReel(
      accessToken: token,
      videoFile: File(video.path),
      caption: 'Check this out!',
      quality: quality,
    );
    print('Upload success: ${result['post']['id']}');
  }
}
```

### 4. Upload Avatar
```dart
Future<Map<String, dynamic>> uploadAvatar({
  required String accessToken,
  required File avatarFile,
}) async {
  var request = http.MultipartRequest(
    'POST',
    Uri.parse('$baseUrl/upload-avatar'),
  );

  request.headers['Authorization'] = 'Bearer $accessToken';
  request.files.add(
    await http.MultipartFile.fromPath('avatar', avatarFile.path),
  );

  var response = await request.send();
  var responseData = await response.stream.bytesToString();
  
  if (response.statusCode == 200) {
    return jsonDecode(responseData);
  }
  throw Exception('Failed to upload avatar');
}
```

### 5. Quality Selection UI
```dart
class QualitySelector extends StatelessWidget {
  final Function(String) onQualitySelected;

  QualitySelector({required this.onQualitySelected});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('Select Quality'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            title: Text('High Quality'),
            subtitle: Text('Best quality, larger file size'),
            leading: Icon(Icons.hd),
            onTap: () {
              onQualitySelected('HIGH');
              Navigator.pop(context);
            },
          ),
          ListTile(
            title: Text('Medium Quality'),
            subtitle: Text('Balanced quality and size'),
            leading: Icon(Icons.high_quality),
            onTap: () {
              onQualitySelected('MEDIUM');
              Navigator.pop(context);
            },
          ),
          ListTile(
            title: Text('Low Quality'),
            subtitle: Text('Smaller file, faster upload'),
            leading: Icon(Icons.sd),
            onTap: () {
              onQualitySelected('LOW');
              Navigator.pop(context);
            },
          ),
        ],
      ),
    );
  }
}
```

### 6. Optimized Image Display
```dart
import 'package:cached_network_image/cached_network_image.dart';

class OptimizedImage extends StatelessWidget {
  final String imageUrl;
  final double? width;
  final double? height;

  OptimizedImage({
    required this.imageUrl,
    this.width,
    this.height,
  });

  @override
  Widget build(BuildContext context) {
    return CachedNetworkImage(
      imageUrl: imageUrl,
      width: width,
      height: height,
      fit: BoxFit.cover,
      placeholder: (context, url) => Container(
        color: Colors.grey[200],
        child: Center(child: CircularProgressIndicator()),
      ),
      errorWidget: (context, url, error) => Icon(Icons.error),
      // Progressive loading
      progressIndicatorBuilder: (context, url, progress) {
        return CircularProgressIndicator(
          value: progress.progress,
        );
      },
    );
  }
}
```

## Performance Tips

### For Developers:

1. **Choose Right Quality:**
   - HIGH: WiFi, important posts
   - MEDIUM: Default for most cases
   - LOW: Data saving mode

2. **Compress Before Upload:**
   ```dart
   // Flutter image compression
   import 'package:image/image.dart' as img;
   
   File compressImage(File file) {
     final image = img.decodeImage(file.readAsBytesSync());
     final compressed = img.encodeJpg(image!, quality: 85);
     return File('compressed.jpg')..writeAsBytesSync(compressed);
   }
   ```

3. **Show Upload Progress:**
   ```dart
   var request = http.MultipartRequest(...);
   var streamedResponse = await request.send();
   
   streamedResponse.stream.listen(
     (chunk) {
       final progress = chunk.length / totalBytes;
       print('Upload progress: ${(progress * 100).toStringAsFixed(0)}%');
     },
   );
   ```

4. **Lazy Load Images:**
   - Load thumbnails first
   - Load full images when visible
   - Use CachedNetworkImage

5. **Handle Network Errors:**
   ```dart
   try {
     await uploadImage(...);
   } on SocketException {
     // No internet
   } on TimeoutException {
     // Upload timeout
   } catch (e) {
     // Other errors
   }
   ```

## Best Practices

✅ **Always use quality parameter** - Don't rely on defaults  
✅ **Compress before upload** - Save bandwidth and time  
✅ **Show upload progress** - Better UX  
✅ **Cache images** - Reduce network requests  
✅ **Handle errors gracefully** - Retry mechanism  
✅ **Validate file size** - Before upload  
✅ **Use progressive loading** - Blur → Sharp  
✅ **Lazy load images** - Load when needed  

## Database Impact

Media files now include additional metadata:
```kotlin
data class MediaFile(
    val fileUrl: String,
    val fileType: MediaType,
    val orderIndex: Int,
    val cloudinaryPublicId: String?,
    val duration: Int?,           // For videos
    val thumbnailUrl: String?     // For videos
    // New fields from optimization:
    // - width, height (from upload result)
    // - format (jpg, webp, mp4)
    // - size (in bytes)
)
```

## Cost Optimization

Cloudinary costs based on:
- Transformations
- Bandwidth
- Storage

**Recommendations:**
- Use `quality: auto` (Cloudinary auto-optimizes)
- Enable CDN caching
- Delete unused media
- Use responsive versions
- Implement lazy loading

## Testing

### Test upload with quality
```bash
# HIGH quality image
curl -X POST http://localhost:8081/api/posts/upload-single \
  -H "Authorization: Bearer {token}" \
  -F "image=@test.jpg" \
  -F "quality=HIGH"

# MEDIUM quality video
curl -X POST http://localhost:8081/api/reels/upload \
  -H "Authorization: Bearer {token}" \
  -F "video=@test.mp4" \
  -F "quality=MEDIUM"
```

## Summary

✅ **3 Quality Levels** - HIGH, MEDIUM, LOW  
✅ **Smart Optimization** - Auto format, compression  
✅ **Multiple Versions** - Main, mobile, thumbnail  
✅ **Face Detection** - Avatar auto-crop  
✅ **Video Streaming** - HLS support  
✅ **Progressive Loading** - Blur → Sharp  
✅ **Responsive Images** - DPR auto  
✅ **Format Auto** - WebP/JPEG selection  

Media tối ưu = Faster load + Better quality + Happy users! 🚀
