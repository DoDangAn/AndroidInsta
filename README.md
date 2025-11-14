# AndroidInsta - Instagram Clone with Flutter + Spring Boot Kotlin

🚀 **Dự án Instagram clone hoàn chỉnh với authentication JWT và REST API**

---

## 💡 MỞ CẢ FRONTEND VÀ BACKEND CÙNG LÚC

**👉 Sử dụng VS Code Workspace để làm việc hiệu quả nhất:**

```bash
# Mở workspace trong VS Code
code AndroidInsta.code-workspace
```

📖 **Xem hướng dẫn:** [GETTING_STARTED.md](GETTING_STARTED.md) | [WORKSPACE_GUIDE.md](WORKSPACE_GUIDE.md)

**Workspace cho phép bạn:**
- ✅ Mở cả Flutter và Spring Boot trong một cửa sổ
- ✅ Tìm kiếm code trong cả FE và BE
- ✅ Debug đồng thời frontend và backend
- ✅ Chạy tasks và commands dễ dàng
- ✅ Sync settings và extensions cho toàn team

---

## ✨ Tính năng đã triển khai

### 🔐 **Authentication System:**
- ✅ User Registration & Login
- ✅ JWT Access & Refresh Tokens  
- ✅ Password Encryption (BCrypt)
- ✅ Role-based Authorization (USER/ADMIN)
- ✅ Token Refresh & Logout
- ✅ Change Password

### 📱 **API Features:**
- ✅ RESTful API với Spring Boot
- ✅ JWT Security Filter
- ✅ Input Validation
- ✅ CORS Support
- ✅ Error Handling
- ✅ Database Integration (H2)

## 🏗️ Cấu trúc dự án

```
AndroidInsta/
├── flutter_app/                    # Flutter mobile application
│   ├── lib/
│   │   ├── main.dart              # Flutter app chính
│   │   └── database/
│   │       └── database_helper.dart
│   └── pubspec.yaml               # Dependencies của Flutter
├── spring_boot_backend/           # Spring Boot Kotlin backend
│   ├── src/main/kotlin/com/androidinsta/
│   │   ├── Model/                 # JPA Entities
│   │   │   ├── User.kt           # User model với JWT
│   │   │   ├── Role.kt           # User roles
│   │   │   ├── Post.kt           # Posts với media files
│   │   │   ├── Comment.kt        # Comments system
│   │   │   ├── Like.kt           # Likes system
│   │   │   ├── Follow.kt         # Follow system
│   │   │   └── ...               # Các models khác
│   │   ├── Repository/           # Data access layer
│   │   │   ├── UserRepository.kt
│   │   │   ├── RoleRepository.kt
│   │   │   └── RefreshTokenRepository.kt
│   │   ├── Service/              # Business logic
│   │   │   └── AuthService.kt    # Authentication service
│   │   ├── controller/           # REST Controllers
│   │   │   ├── AuthController.kt # Auth endpoints
│   │   │   └── TestController.kt # Health check
│   │   ├── config/               # Security & JWT config
│   │   │   ├── SecurityConfig.kt # Spring Security
│   │   │   ├── JwtUtil.kt        # JWT utilities
│   │   │   ├── JwtAuthenticationFilter.kt
│   │   │   └── ...               # Các config khác
│   │   └── dto/                  # Data Transfer Objects
│   │       ├── AuthDto.kt        # Auth DTOs
│   │       └── UserDto.kt        # User DTOs
│   ├── build.gradle.kts          # Gradle build với JWT deps
│   ├── API_DOCS.md               # 📚 API Documentation
│   ├── TEST_API.md               # 🧪 Test Scripts
│   └── src/main/resources/
│       └── application.properties # App config với JWT
├── AUTHENTICATION_SUMMARY.md      # 📋 Tổng quan authentication
└── README.md
```

## Yêu cầu hệ thống

### Cho Flutter:
- Flutter SDK (>= 3.0.0)
- Dart SDK
- Android Studio hoặc VS Code với Flutter extension
- Android SDK (cho Android development)
- Xcode (cho iOS development trên macOS)

### Cho Spring Boot:
- Java 17 hoặc cao hơn
- Gradle (hoặc sử dụng Gradle Wrapper có sẵn)

## Cài đặt và chạy

### 1. Cài đặt Flutter
```bash
# Tải Flutter SDK từ: https://flutter.dev/docs/get-started/install
# Thêm Flutter vào PATH
# Kiểm tra cài đặt:
flutter doctor
```

### 2. Chạy Spring Boot backend
```bash
cd spring_boot_backend
./gradlew bootRun
# Hoặc trên Windows:
gradlew.bat bootRun
```

Backend sẽ chạy trên: http://localhost:8080

### 3. Chạy Flutter app
```bash
cd flutter_app
flutter pub get
flutter run
```

## API Endpoints

- `GET /api/hello` - Test connection endpoint
- `GET /api/posts` - Lấy danh sách posts
- `POST /api/posts` - Tạo post mới

## Features

- ✅ Flutter mobile app với Material Design
- ✅ Spring Boot REST API với Kotlin
- ✅ Cross-origin support cho Flutter
- ✅ HTTP client integration
- ✅ Sample API endpoints
- ✅ H2 in-memory database
- ✅ RESTful architecture

## Development

### Database Console
Khi backend đang chạy, có thể truy cập H2 console tại:
http://localhost:8080/h2-console

### API Testing
Có thể test API bằng:
- Postman
- cURL
- Flutter app (button "Test API")

## TODO

- [ ] Thêm authentication
- [ ] Implement database models
- [ ] Add more Flutter screens
- [ ] Add error handling
- [ ] Add logging
- [ ] Docker containerization
- [ ] Unit tests

## Ghi chú

Đây là project template cơ bản. Trong thực tế, bạn cần:
1. Cài đặt authentication/authorization
2. Sử dụng database thực (PostgreSQL, MySQL...)
3. Thêm validation và error handling
4. Implement business logic phù hợp
5. Add tests
6. Set up CI/CD