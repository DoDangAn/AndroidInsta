# AndroidInsta Project - Development Guide

## 📦 Project Structure

```
AndroidInsta/
├── flutter_app/              # Flutter Mobile App (Frontend)
│   ├── lib/
│   │   ├── config/          # API configuration
│   │   ├── models/          # Data models
│   │   ├── screens/         # UI screens
│   │   ├── services/        # API services
│   │   └── main.dart
│   └── android/
│
└── spring_boot_backend/      # Spring Boot API (Backend)
    ├── src/main/kotlin/
    │   └── com/androidinsta/
    │       ├── controller/
    │       ├── Service/
    │       ├── Repository/
    │       ├── Model/
    │       ├── dto/
    │       └── config/
    ├── docker-compose.yml    # MySQL, Redis, Kafka
    └── build.gradle.kts
```

## 🚀 Quick Start

### 1. Start Docker Services
```powershell
cd D:\AndroidInsta\spring_boot_backend
docker-compose up -d
```

**Services:**
- MySQL: localhost:3306 (root/root)
- Redis: localhost:6379
- Kafka: localhost:9092
- Zookeeper: localhost:2181
- Kafka UI: localhost:8080

### 2. Start Backend
```powershell
cd D:\AndroidInsta\spring_boot_backend
Push-Location D:\AndroidInsta\spring_boot_backend
& .\gradlew.bat bootRun
```

Backend will run at: http://localhost:8081

### 3. Start Flutter App
```powershell
cd D:\AndroidInsta\flutter_app
flutter run
```

## 🔧 Configuration Files

### Backend: `application.properties`
```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/android_insta?allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.kafka.bootstrap-servers=localhost:9092
spring.data.redis.host=localhost
```

### Frontend: `lib/config/api_config.dart`
```dart
static const String baseUrl = 'http://10.0.2.2:8081'; // Android Emulator
```

## 📡 API Endpoints

### Public Endpoints (No Auth Required)
- POST `/api/auth/login`
- POST `/api/auth/register`

### Protected Endpoints (Require JWT Token)
- GET `/api/auth/me`
- POST `/api/auth/logout`
- GET `/api/users/*`
- GET `/api/posts/*`
- WebSocket `/ws`

## 🔐 Authentication Flow

1. User login/register → Backend returns JWT tokens
2. Flutter saves tokens to SharedPreferences
3. All API calls include: `Authorization: Bearer {token}`
4. Backend validates JWT using `SecurityConfig`

## 🐛 Common Issues & Solutions

### Backend won't start
**Issue:** Port 3306 already in use  
**Solution:**
```powershell
taskkill /PID 7060 /F  # Kill MySQL process
docker-compose down -v
docker-compose up -d
```

**Issue:** Kafka/Zookeeper fails  
**Solution:** Use wurstmeister images in docker-compose.yml

### Flutter app can't connect
**Issue:** Network error  
**Solution:**
- Check backend is running: http://localhost:8081/actuator/health
- Android Emulator: Use `10.0.2.2` not `localhost`
- Physical device: Use computer's IP address

### Build errors
**Solution:**
```bash
# Flutter
flutter clean
flutter pub get
flutter run

# Backend
./gradlew clean build
```

## 📝 Demo Credentials

```
Username: testuser
Password: password123
```

## ⚙️ Gradle Commands

```bash
# Build without tests
./gradlew build -x test

# Run app
./gradlew bootRun

# Clean build
./gradlew clean build
```

## 🐳 Docker Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker logs androidinsta-mysql
docker logs androidinsta-kafka

# Check containers
docker ps
```

## 📱 Flutter Commands

```bash
# Get dependencies
flutter pub get

# Run on emulator
flutter run

# Build APK
flutter build apk

# Clean cache
flutter clean

# Check devices
flutter devices
```

## 🔍 Debugging

### Backend Logs
- Check terminal running `gradlew bootRun`
- Look for Spring Boot startup logs
- Kafka consumer connections
- WebSocket connections

### Flutter Logs
- Check console output from `flutter run`
- Use `print()` statements
- DevTools: http://localhost:9102

### Database
```sql
mysql -h localhost -u root -proot android_insta
SHOW TABLES;
SELECT * FROM users;
```

## 📊 Project Status

✅ **Completed:**
- Backend API với Spring Boot + Kotlin
- MySQL, Redis, Kafka integration
- JWT Authentication
- WebSocket real-time chat
- Flutter screens (Login, Register, Home, Profile, Chat)
- API services và models
- Điều hướng (navigation)

⏳ **In Progress:**
- Post creation với camera/gallery
- Image upload
- Notifications
- Search functionality

## 🎯 Next Steps

1. Test toàn bộ flow: Register → Login → Home → Profile → Chat
2. Kiểm tra WebSocket real-time messaging
3. Test follow/unfollow functionality
4. Thêm error handling tốt hơn
5. Implement remaining features

---

**Last Updated:** November 3, 2025  
**Version:** 1.0.0
