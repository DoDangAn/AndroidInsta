# 🎯 VS Code Workspace - Quick Reference

## 📖 Mở Workspace

```bash
# Command line
code AndroidInsta.code-workspace

# Hoặc trong VS Code
File → Open Workspace from File... → AndroidInsta.code-workspace
```

## ⌨️ Phím tắt quan trọng

| Phím tắt | Chức năng |
|----------|-----------|
| `Ctrl+P` / `Cmd+P` | Quick Open - Mở file nhanh |
| `Ctrl+Shift+F` / `Cmd+Shift+F` | Tìm kiếm trong toàn bộ workspace |
| `Ctrl+Shift+P` / `Cmd+Shift+P` | Command Palette |
| `Ctrl+Shift+D` / `Cmd+Shift+D` | Mở Debug panel |
| `F5` | Start Debugging |
| `Ctrl+Shift+B` / `Cmd+Shift+B` | Run Build Task |
| `Ctrl+` ` | Toggle Terminal |
| `Ctrl+\` / `Cmd+\` | Split Editor |

## 🏃 Chạy Project

### Backend (Spring Boot)
```bash
# Trong terminal
cd spring_boot_backend
./gradlew bootRun

# Hoặc dùng Task
Ctrl+Shift+P → Tasks: Run Task → Spring Boot: Run
```

### Frontend (Flutter)
```bash
# Trong terminal
cd flutter_app
flutter run

# Hoặc dùng Debug
F5 → Chọn "Flutter: Run"
```

### Cả hai cùng lúc
```
Ctrl+Shift+D → Chọn "Full Stack: FE + BE" → F5
```

## 📂 Cấu trúc Folders

- **🎨 Flutter Frontend** - Code Flutter
- **⚙️ Spring Boot Backend** - Code Kotlin/Spring Boot  
- **📁 Root** - Docker, README, configs

## 🔍 Tìm kiếm nhanh

```
# Mở file
Ctrl+P → gõ tên file

# Ví dụ:
AuthController.kt      → Backend controller
login_screen.dart      → Frontend screen
UserDto.kt            → Backend DTO
user_service.dart     → Frontend service
```

## 🐛 Debug

### Debug Backend
```
1. Đặt breakpoint trong file .kt
2. F5 → Chọn "Spring Boot: Run"
```

### Debug Frontend
```
1. Đặt breakpoint trong file .dart
2. F5 → Chọn "Flutter: Debug"
```

### Debug cả hai
```
1. F5 → Chọn "Full Stack: FE + BE"
2. Đặt breakpoints ở cả FE và BE
3. Code sẽ dừng ở breakpoint khi được execute
```

## 🛠️ Tasks có sẵn

`Ctrl+Shift+P` → `Tasks: Run Task`

- **Flutter: Get Dependencies** - `flutter pub get`
- **Flutter: Build APK** - Build Android app
- **Spring Boot: Build** - Build backend
- **Spring Boot: Run** - Chạy server
- **Start Full Stack** - Khởi động cả hai

## 💡 Tips

### 1. Multiple Terminals
```
Ctrl+Shift+` - Mở terminal mới
Mỗi terminal có thể cd vào folder khác nhau
```

### 2. Split Editor
```
Ctrl+\ - Split editor
Xem code FE và BE side-by-side
```

### 3. Focus vào folder
```
Click vào folder name trong Explorer
Ctrl+Shift+F - Chỉ search trong folder đó
```

### 4. Git operations
```
Ctrl+Shift+G - Mở Source Control
Có thể commit changes từ các folders khác nhau
```

## 📦 Extensions đề xuất

Khi mở workspace lần đầu, install các extensions:

**Essential:**
- Dart & Flutter
- Kotlin Language
- Java Extension Pack
- Spring Boot Extension Pack

**Recommended:**
- GitLens
- Thunder Client (test API)
- Prettier

## 🔧 Customize

Edit `AndroidInsta.code-workspace` để:
- Thay đổi settings
- Thêm tasks mới
- Thêm launch configurations
- Thêm folders khác

## ❓ Troubleshooting

### Extensions không load?
```
Ctrl+Shift+P → Developer: Reload Window
```

### Flutter không tìm thấy SDK?
```
Ctrl+, → Search "flutter sdk" → Set path
```

### Java/Kotlin không hoạt động?
```
Cài Java Extension Pack
Restart VS Code
```

### Tasks không chạy?
```
Kiểm tra terminal có đúng cwd không
Check file permissions (gradlew phải executable)
```

## 📚 Đọc thêm

- [WORKSPACE_GUIDE.md](WORKSPACE_GUIDE.md) - Hướng dẫn chi tiết
- [README.md](README.md) - Project overview
- [VS Code Workspace Docs](https://code.visualstudio.com/docs/editor/workspaces)

---

**Happy Coding! 🚀**
