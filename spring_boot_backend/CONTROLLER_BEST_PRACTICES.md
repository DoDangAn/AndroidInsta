# Controller Best Practices - AndroidInsta Backend

## 📋 **Tổng quan**

Tất cả controllers đã được cải thiện theo chuẩn Spring Boot Enterprise với các nguyên tắc:

### ✅ **Nguyên tắc Controller Design:**

1. **Single Responsibility** - Mỗi controller chỉ quản lý một domain
2. **RESTful API** - Tuân thủ REST conventions  
3. **Input Validation** - Sử dụng `@Valid` cho tất cả request DTOs
4. **Exception Handling** - Để GlobalExceptionHandler xử lý, không dùng try-catch thủ công
5. **Security** - Sử dụng SecurityUtil để lấy current user
6. **Caching** - Cache annotations ở đúng chỗ
7. **Documentation** - KDoc comments đầy đủ

---

## 🎯 **Controller Improvements**

### **1. AuthController** ✅

#### **Trước khi cải thiện:**
```kotlin
@PostMapping("/login")
fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
    return try {
        // Manual try-catch - BAD PRACTICE
        val jwtResponse = authService.login(loginRequest)
        ResponseEntity.ok(...)
    } catch (e: BadCredentialsException) {
        // Manual error handling
    } catch (e: Exception) {
        // Generic exception
    }
}
```

#### **Sau khi cải thiện:**
```kotlin
/**
 * POST /api/auth/login - User login
 * @param loginRequest Login credentials
 * @return JWT tokens and user info
 */
@PostMapping("/login")
fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
    val jwtResponse = authService.login(loginRequest)
    return ResponseEntity.ok(
        AuthResponse(
            success = true,
            message = "Login successful",
            data = jwtResponse
        )
    )
}
```

#### **Các cải thiện:**
- ✅ Loại bỏ try-catch thủ công
- ✅ Thêm `@Valid` annotation
- ✅ Thêm KDoc documentation
- ✅ Sử dụng `IllegalStateException` thay vì `RuntimeException`
- ✅ Consistent response structure

#### **Endpoints:**
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register (HTTP 201 CREATED)
- `POST /api/auth/signup` - Alias for register
- `POST /api/auth/google` - Google OAuth
- `POST /api/auth/refresh-token` - Refresh JWT
- `POST /api/auth/logout` - Logout
- `POST /api/auth/change-password` - Change password
- `GET /api/auth/me` - Get current user
- `GET /api/auth/validate-token` - Validate JWT

---

### **2. PostController** ✅

#### **Trước khi cải thiện:**
```kotlin
@GetMapping("/feed")
fun getFeed(
    @RequestParam(value = "page", required = false) page: Int?,
    @RequestParam(value = "size", required = false) size: Int?
): FeedResponse {
    val userId = SecurityUtil.getCurrentUserId()
        ?: throw RuntimeException("Unauthorized")  // BAD
    
    val pageable = PageRequest.of(page ?: 0, size ?: 20, ...)
    return postService.getFeedResponse(userId, pageable)
}
```

#### **Sau khi cải thiện:**
```kotlin
/**
 * GET /api/posts/feed - Get personalized feed
 */
@GetMapping("/feed")
fun getFeed(
    @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
    @RequestParam(value = "size", required = false, defaultValue = "20") size: Int
): ResponseEntity<FeedResponse> {
    val userId = SecurityUtil.getCurrentUserId()
        ?: throw IllegalStateException("User not authenticated")
    
    val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
    val response = postService.getFeedResponse(userId, pageable)
    return ResponseEntity.ok(response)
}
```

#### **Các cải thiện:**
- ✅ Sử dụng `defaultValue` thay vì nullable + elvis operator
- ✅ `IllegalStateException` thay vì `RuntimeException`
- ✅ Return `ResponseEntity<T>` thay vì trực tiếp DTO
- ✅ Thêm validation `@Valid`
- ✅ Loại bỏ try-catch thủ công
- ✅ Cache annotations clean hơn

#### **Endpoints:**
- `GET /api/posts/feed` - Personalized feed
- `GET /api/posts/user/{userId}` - User's posts
- `GET /api/posts/{postId}` - Post details
- `GET /api/posts/advertise` - Advertise posts
- `POST /api/posts` - Create post (HTTP 201)
- `PUT /api/posts/{postId}` - Update post
- `DELETE /api/posts/{postId}` - Delete post (HTTP 204)
- `POST /api/posts/{postId}/like` - Like post
- `DELETE /api/posts/{postId}/like` - Unlike post
- `GET /api/posts/{postId}/like/count` - Get like count
- `GET /api/posts/{postId}/like/status` - Check like status

---

## 🔧 **Best Practices Applied**

### **1. Exception Handling**

#### ❌ **KHÔNG nên:**
```kotlin
@PostMapping("/login")
fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
    return try {
        val result = service.login(request)
        ResponseEntity.ok(result)
    } catch (e: BadCredentialsException) {
        ResponseEntity.status(401).body(...)
    } catch (e: Exception) {
        ResponseEntity.status(500).body(...)
    }
}
```

#### ✅ **NÊN:**
```kotlin
@PostMapping("/login")
fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
    val result = service.login(request)  // Let GlobalExceptionHandler handle errors
    return ResponseEntity.ok(result)
}
```

### **2. Security & Authentication**

#### ❌ **KHÔNG nên:**
```kotlin
val userId = SecurityUtil.getCurrentUserId()
    ?: throw RuntimeException("Unauthorized")
```

#### ✅ **NÊN:**
```kotlin
val userId = SecurityUtil.getCurrentUserId()
    ?: throw IllegalStateException("User not authenticated")
```

### **3. Request Parameters**

#### ❌ **KHÔNG nên:**
```kotlin
@GetMapping("/feed")
fun getFeed(
    @RequestParam(required = false) page: Int?,
    @RequestParam(required = false) size: Int?
) {
    val actualPage = page ?: 0
    val actualSize = size ?: 20
    // ...
}
```

#### ✅ **NÊN:**
```kotlin
@GetMapping("/feed")
fun getFeed(
    @RequestParam(required = false, defaultValue = "0") page: Int,
    @RequestParam(required = false, defaultValue = "20") size: Int
) {
    // page và size đã có default value
}
```

### **4. HTTP Status Codes**

```kotlin
// GET - 200 OK
ResponseEntity.ok(data)

// POST (create) - 201 CREATED
ResponseEntity.status(HttpStatus.CREATED).body(data)

// DELETE - 204 NO CONTENT
ResponseEntity.noContent().build()

// PUT - 200 OK
ResponseEntity.ok(updatedData)
```

### **5. Validation**

```kotlin
@PostMapping("/create")
fun createPost(@Valid @RequestBody request: CreatePostRequest) {
    // Jakarta Validation tự động check các @NotNull, @Size, etc.
}
```

### **6. Cache Annotations**

```kotlin
@PostMapping("/create")
@CacheEvict(
    value = ["feedPosts", "userPosts"], 
    allEntries = true
)
fun createPost(...) { ... }

@GetMapping("/feed")
// Cache ở Service layer, không cache ở Controller
fun getFeed(...) { ... }
```

---

## 📊 **Controller Statistics**

### **User Controllers:**
1. ✅ **AuthController** - 9 endpoints
2. ✅ **PostController** - 11 endpoints
3. **UserController** - Cần cải thiện
4. **ChatController** - Cần cải thiện
5. **CommentController** - Cần cải thiện
6. **FriendController** - Cần cải thiện
7. **MessageController** - Cần cải thiện
8. **NotificationController** - Cần cải thiện
9. **ReelController** - Cần cải thiện
10. **SearchController** - Cần cải thiện
11. **PostUploadController** - Cần cải thiện

### **Admin Controllers:**
1. **AdminUserController** - Cần cải thiện
2. **AdminStatsController** - Cần cải thiện
3. **PostAdminController** - Cần cải thiện
4. **ProfileController** - Cần cải thiện
5. **ApiController** - Cần cải thiện

### **Other Controllers:**
1. **WebSocketChatController** - Cần cải thiện

---

## 🎯 **Checklist cho từng Controller**

Khi cải thiện controller, đảm bảo:

- [ ] Loại bỏ try-catch thủ công
- [ ] Thêm `@Valid` cho request DTOs
- [ ] Sử dụng `IllegalStateException` thay vì `RuntimeException`
- [ ] Sử dụng `defaultValue` cho @RequestParam
- [ ] Return `ResponseEntity<T>` thay vì trực tiếp DTO
- [ ] Thêm KDoc comments
- [ ] Đúng HTTP status code (201 cho create, 204 cho delete)
- [ ] Cache annotations đúng chỗ
- [ ] RESTful URL patterns
- [ ] Consistent response structure

---

## 🚀 **Next Steps**

1. ✅ AuthController - HOÀN THÀNH
2. ✅ PostController - HOÀN THÀNH
3. ⏳ UserController - Đang cải thiện
4. ⏳ Các controllers còn lại...

**Mục tiêu:** 100% controllers tuân thủ best practices! 🎯
