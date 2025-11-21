# 📂 Hướng dẫn sử dụng Workspace (FE + BE)

## 🎯 Mục đích

Workspace này được tạo ra để bạn có thể **mở và làm việc đồng thời** với cả **Frontend (Flutter)** và **Backend (Spring Boot Kotlin)** trong cùng một cửa sổ VS Code.

## 🚀 Cách mở Workspace

### Phương pháp 1: Mở từ VS Code
1. Mở VS Code
2. Chọn `File` → `Open Workspace from File...`
3. Chọn file `AndroidInsta.code-workspace` trong thư mục gốc của project
4. Workspace sẽ mở với 3 folders:
   - 🎨 **Flutter Frontend** - Code Flutter của bạn
   - ⚙️ **Spring Boot Backend** - Code Spring Boot Kotlin
   - 📁 **Root** - Thư mục gốc (cho docker-compose, README, etc.)

### Phương pháp 2: Mở từ Command Line
```bash
code AndroidInsta.code-workspace
```

### Phương pháp 3: Double-click (Windows/MacOS)
Chỉ cần double-click vào file `AndroidInsta.code-workspace`

## 📁 Cấu trúc Workspace

```
VS Code Workspace
├── 🎨 Flutter Frontend (flutter_app/)
│   ├── lib/
│   │   ├── main.dart
│   │   ├── screens/
│   │   ├── services/
│   │   ├── models/
│   │   └── config/
│   └── pubspec.yaml
│
├── ⚙️ Spring Boot Backend (spring_boot_backend/)
│   ├── src/main/kotlin/
│   │   └── com/androidinsta/
│   │       ├── controller/
│   │       ├── Model/
│   │       ├── Service/
│   │       ├── Repository/
│   │       ├── config/
│   │       └── dto/
│   └── build.gradle.kts
│
└── 📁 Root
    ├── README.md
    ├── docker-compose.yml
    └── AndroidInsta.code-workspace
```

## ⚙️ Tính năng của Workspace

### 1. 🔍 Tìm kiếm trong cả FE và BE
- Sử dụng `Ctrl+Shift+F` (Windows/Linux) hoặc `Cmd+Shift+F` (Mac)
- Tìm kiếm sẽ được thực hiện trong cả Flutter và Spring Boot code

### 2. 🏃 Chạy cả Frontend và Backend cùng lúc
Workspace có sẵn cấu hình để chạy cả 2:

#### Chạy Backend:
- Mở Command Palette: `Ctrl+Shift+P` / `Cmd+Shift+P`
- Chọn `Tasks: Run Task`
- Chọn `Spring Boot: Run`

#### Chạy Frontend:
- Nhấn `F5` hoặc đi đến Debug panel
- Chọn `Flutter: Run` từ dropdown
- Nhấn Start

#### Chạy cả hai cùng lúc:
- Đi đến Debug panel (Ctrl+Shift+D)
- Chọn `Full Stack: FE + BE` từ dropdown
- Nhấn Start (F5)

### 3. 📝 Settings được cấu hình sẵn
- **Auto-format on save** cho cả Dart và Kotlin
- **Line length** 120 characters cho Dart
- **Ẩn các folder không cần thiết** (.dart_tool, .gradle, build, etc.)
- **File associations** đúng cho .dart và .kt files

### 4. 🔌 Extensions được đề xuất
Khi mở workspace lần đầu, VS Code sẽ đề xuất cài đặt:

**Flutter & Dart:**
- Dart
- Flutter

**Kotlin & Java:**
- Kotlin Language
- Java Extension Pack
- Spring Boot Extension Pack
- Spring Boot Dashboard

**General:**
- GitLens
- Prettier
- Thunder Client (REST API testing)

### 5. 🎯 Tasks có sẵn
Chạy tasks qua Command Palette (`Ctrl+Shift+P` → `Tasks: Run Task`):

| Task | Mô tả |
|------|-------|
| `Flutter: Get Dependencies` | Chạy `flutter pub get` |
| `Flutter: Build APK` | Build Android APK |
| `Spring Boot: Build` | Build backend với Gradle |
| `Spring Boot: Run` | Chạy Spring Boot server |
| `Start Full Stack` | Khởi động cả FE và BE |

## 💡 Mẹo sử dụng

### 1. 🔀 Chuyển đổi giữa files FE và BE
- Sử dụng `Ctrl+P` / `Cmd+P` để mở Quick Open
- Gõ tên file, ví dụ:
  - `login_screen.dart` → mở Flutter screen
  - `AuthController.kt` → mở Spring Boot controller
  - `UserDto.kt` → mở backend DTO
  - `user_service.dart` → mở Flutter service

### 2. 🔍 Tìm kiếm theo folder
- Click vào folder name trong Explorer sidebar
- Nhấn chuột phải → `Find in Folder...`
- Chỉ tìm trong folder đó thôi

### 3. 📊 Terminal cho mỗi project
- Mở nhiều terminal tabs
- Mỗi terminal có thể cd vào flutter_app hoặc spring_boot_backend
- Hoặc sử dụng split terminal

### 4. 🎨 Customize workspace
Bạn có thể chỉnh sửa file `.code-workspace` để:
- Thêm folders khác
- Thay đổi settings
- Thêm tasks hoặc launch configurations

## 🐛 Debug cả Frontend và Backend

### Debug Frontend (Flutter):
1. Đặt breakpoint trong file .dart
2. Chọn `Flutter: Debug` trong Debug panel
3. Nhấn F5
4. App sẽ chạy ở debug mode

### Debug Backend (Spring Boot):
1. Đặt breakpoint trong file .kt
2. Chọn `Spring Boot: Run` trong Debug panel
3. Nhấn F5
4. Backend sẽ chạy với debugger attached

### Debug đồng thời:
1. Chọn `Full Stack: FE + BE` trong Debug panel
2. Nhấn F5
3. Cả hai sẽ chạy và bạn có thể debug cả hai cùng lúc

## 📋 Workflow đề xuất

### 1. Làm việc với một tính năng mới
```
1. Mở workspace → AndroidInsta.code-workspace
2. Tạo API endpoint trong Spring Boot (Backend)
3. Test API với Thunder Client hoặc Postman
4. Implement UI trong Flutter (Frontend)
5. Integrate Flutter với API
6. Debug cả hai nếu cần
```

### 2. Fix bug xuyên suốt FE và BE
```
1. Mở workspace
2. Search toàn bộ code với Ctrl+Shift+F
3. Tìm được bug ở đâu (FE hay BE)
4. Fix cả hai phía nếu cần
5. Test lại
```

## 🎓 Ví dụ thực tế

### Ví dụ 1: Thêm tính năng Like post
```
Backend (Spring Boot):
├── Model/Like.kt
├── Repository/LikeRepository.kt  
├── Service/LikeService.kt
└── controller/LikeController.kt

Frontend (Flutter):
├── models/like_model.dart
├── services/like_service.dart
└── screens/post_screen.dart (thêm nút like)
```

Với workspace, bạn có thể:
1. Mở tất cả files này cùng lúc
2. Code backend trước
3. Test API
4. Code frontend sau
5. Không cần đóng/mở project

### Ví dụ 2: Debug authentication flow
```
1. Đặt breakpoint ở AuthController.kt (BE)
2. Đặt breakpoint ở login_service.dart (FE)
3. Run cả hai ở debug mode
4. Test login từ app
5. Xem request đi từ FE đến BE
6. Debug từng bước
```

## ❓ FAQ

### Q: Tôi có cần mở workspace mỗi khi code không?
**A:** Có, để tận dụng các tính năng. Nhưng bạn vẫn có thể mở từng folder riêng lẻ nếu muốn.

### Q: Tôi có thể thêm folder khác vào workspace không?
**A:** Có! Edit file `.code-workspace` và thêm vào array `folders`.

### Q: Workspace này hoạt động với các IDE khác không?
**A:** File `.code-workspace` là định dạng của VS Code. Các IDE khác (IntelliJ, Android Studio) có cơ chế workspace riêng.

### Q: Tôi có thể commit file workspace vào Git không?
**A:** Có! File này được commit để team cùng sử dụng.

### Q: Extensions có được tự động cài đặt không?
**A:** Không, VS Code chỉ đề xuất. Bạn phải click "Install" để cài.

## 🎉 Kết luận

Với workspace này, bạn có thể:
- ✅ **Mở cả FE và BE cùng lúc**
- ✅ **Tìm kiếm nhanh trong toàn bộ codebase**
- ✅ **Debug đồng thời**
- ✅ **Chạy tasks dễ dàng**
- ✅ **Làm việc hiệu quả hơn**

Chúc bạn code vui vẻ! 🚀

---

**Nếu có thắc mắc:** Xem thêm [VS Code Workspace Documentation](https://code.visualstudio.com/docs/editor/workspaces)
