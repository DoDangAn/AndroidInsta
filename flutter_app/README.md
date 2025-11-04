# AndroidInsta Flutter App

Flutter frontend cho ứng dụng mạng xã hội AndroidInsta.

## 🚀 Setup

### 1. Cài đặt dependencies
```bash
flutter pub get
```

### 2. Cấu hình Backend URL

File: `lib/config/api_config.dart`

**Android Emulator:** `http://10.0.2.2:8081`  
**iOS Simulator:** `http://localhost:8081`  
**Physical Device:** `http://192.168.1.x:8081` (thay x bằng IP của máy)

### 3. Chạy Backend
```bash
cd ../spring_boot_backend
./gradlew bootRun
```

### 4. Chạy App
```bash
flutter run
```

## 📱 Features

- ✅ Authentication (Login/Register/Logout)
- ✅ Home Screen với user info
- ✅ Profile Screen (view user, follow/unfollow)
- ✅ Chat (WebSocket real-time messaging)
- ✅ Posts feed & user posts
- ✅ Auto token management with SharedPreferences

## 📡 API Endpoints

- **Auth:** `/api/auth/*` (login, register, me, logout)
- **Users:** `/api/users/*` (profile, follow, stats)
- **Posts:** `/api/posts/*` (feed, create, like, comments)
- **Chat:** `/api/chat/*` + WebSocket

## 🔐 Demo Account

```
Username: testuser
Password: password123
```

## 🐛 Troubleshooting

**Lỗi kết nối backend:**
- Kiểm tra backend đang chạy tại port 8081
- Android Emulator: Dùng `10.0.2.2` thay vì `localhost`

**Lỗi build:**
```bash
flutter clean
flutter pub get
flutter run
```

---

**Author:** AndroidInsta Team | **Updated:** Nov 3, 2025
