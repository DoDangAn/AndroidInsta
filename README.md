# AndroidInsta - Flutter + Spring Boot Kotlin Project

Dự án demo kết hợp Flutter frontend và Spring Boot backend với Kotlin.

## Cấu trúc dự án

```
AndroidInsta/
├── flutter_app/          # Flutter mobile application
│   ├── lib/
│   │   └── main.dart     # Flutter app chính
│   └── pubspec.yaml      # Dependencies của Flutter
├── spring_boot_backend/   # Spring Boot Kotlin backend
│   ├── src/main/kotlin/
│   │   └── com/androidinsta/
│   │       ├── AndroidInstaApplication.kt
│   │       └── controller/
│   │           └── ApiController.kt
│   ├── build.gradle.kts  # Gradle build file
│   └── src/main/resources/
│       └── application.properties
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