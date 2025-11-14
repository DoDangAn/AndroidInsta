# ✅ Getting Started - Workspace Checklist

## 🚀 Bắt đầu với VS Code Workspace trong 5 phút

### Bước 1: Mở Workspace ✓
```bash
# Cách 1: Command line
cd /path/to/AndroidInsta
code AndroidInsta.code-workspace

# Cách 2: Trong VS Code
File → Open Workspace from File... → chọn AndroidInsta.code-workspace

# Cách 3: Double-click
Double-click vào file AndroidInsta.code-workspace
```

### Bước 2: Cài Extensions (Lần đầu tiên) ✓
Khi mở workspace, VS Code sẽ hiện thông báo:
```
"This workspace recommends extensions..."
```

**→ Nhấn "Install All"** để cài:
- ✅ Dart & Flutter
- ✅ Kotlin Language
- ✅ Java Extension Pack
- ✅ Spring Boot Extensions
- ✅ GitLens
- ✅ Thunder Client (REST API testing)

⏱️ Thời gian: ~2-3 phút

### Bước 3: Kiểm tra Cấu trúc ✓
Sau khi mở workspace, bạn sẽ thấy 3 folders trong Explorer:

```
🎨 Flutter Frontend
  └─ lib/
     ├─ main.dart
     ├─ screens/
     ├─ services/
     └─ models/

⚙️ Spring Boot Backend
  └─ src/main/kotlin/
     ├─ controller/
     ├─ Model/
     ├─ Service/
     └─ Repository/

📁 Root
  ├─ README.md
  ├─ docker-compose.yml
  └─ AndroidInsta.code-workspace
```

### Bước 4: Test Quick Open ✓
Thử tìm file nhanh:
1. Nhấn `Ctrl+P` (Windows/Linux) hoặc `Cmd+P` (Mac)
2. Gõ: `main.dart` → Nhấn Enter
3. Gõ: `AuthController` → Nhấn Enter

✅ Nếu files mở được → Perfect!

### Bước 5: Chạy Backend ✓
```bash
# Option 1: Dùng Terminal
Ctrl+` (mở terminal)
cd spring_boot_backend
./gradlew bootRun

# Option 2: Dùng Task
Ctrl+Shift+P → Tasks: Run Task → Spring Boot: Run
```

Đợi cho đến khi thấy:
```
Started AndroidInstaApplication in X seconds
```

### Bước 6: Chạy Frontend ✓
```bash
# Option 1: Dùng Terminal (terminal mới)
cd flutter_app
flutter run

# Option 2: Dùng Debug
F5 → Chọn "Flutter: Run"
```

### Bước 7: Test Full Stack ✓
1. Nhấn `Ctrl+Shift+D` (mở Debug panel)
2. Chọn "Full Stack: FE + BE" từ dropdown
3. Nhấn `F5` (Start Debugging)

✅ Cả Backend và Frontend sẽ chạy cùng lúc!

---

## 📚 Các thao tác thường dùng

### Tìm kiếm trong toàn bộ code
```
Ctrl+Shift+F → Gõ từ khóa → Enter
```

### Mở file nhanh
```
Ctrl+P → Gõ tên file → Enter
```

### Command Palette
```
Ctrl+Shift+P → Gõ command → Enter
```

### Multiple Terminals
```
Ctrl+Shift+` → Mở terminal mới
```

### Split Editor
```
Ctrl+\ → Split editor thành 2 cột
```

---

## 🎯 Workflow đề xuất

### Scenario 1: Thêm tính năng mới (Full Stack)

1. **Tạo Backend API:**
   ```
   ⚙️ Backend:
   ├─ Model/NewFeature.kt
   ├─ Repository/NewFeatureRepository.kt
   ├─ Service/NewFeatureService.kt
   └─ controller/NewFeatureController.kt
   ```

2. **Test API:**
   - Dùng Thunder Client hoặc Postman
   - Test endpoint: `POST /api/feature`

3. **Tạo Frontend UI:**
   ```
   🎨 Frontend:
   ├─ models/new_feature_model.dart
   ├─ services/new_feature_service.dart
   └─ screens/new_feature_screen.dart
   ```

4. **Integrate:**
   - Call API từ Flutter
   - Test end-to-end

### Scenario 2: Debug một lỗi

1. **Tìm lỗi:**
   ```
   Ctrl+Shift+F → Search "error message"
   ```

2. **Đặt breakpoints:**
   - Click vào line number (bên trái code)
   - Đặt ở cả FE và BE

3. **Run Debug:**
   ```
   F5 → Full Stack: FE + BE
   ```

4. **Debug:**
   - Code sẽ dừng tại breakpoint
   - Xem variables, call stack
   - Step through code

### Scenario 3: Code review

1. **Xem changes:**
   ```
   Ctrl+Shift+G → Source Control
   ```

2. **Review files:**
   - Click vào file để xem diff
   - Review cả FE và BE changes

3. **Commit:**
   ```
   Ghi message → Ctrl+Enter
   ```

---

## ❓ Troubleshooting

### Extensions không hoạt động?
```
1. Ctrl+Shift+P
2. Developer: Reload Window
```

### Flutter SDK không tìm thấy?
```
1. Ctrl+, (Settings)
2. Search: "dart.flutterSdkPath"
3. Set đường dẫn đến Flutter SDK
```

### Java/Kotlin language server lỗi?
```
1. Cài Java Extension Pack
2. Restart VS Code
3. Wait cho language server khởi động (~1 phút)
```

### Tasks không chạy?
```
1. Check terminal cwd (current directory)
2. Đảm bảo gradlew có executable permission:
   chmod +x spring_boot_backend/gradlew
```

### Workspace không hiển thị đúng folders?
```
1. File → Close Workspace
2. Mở lại: File → Open Workspace from File
3. Chọn AndroidInsta.code-workspace
```

---

## 📖 Đọc thêm

- **Chi tiết đầy đủ:** [WORKSPACE_GUIDE.md](WORKSPACE_GUIDE.md)
- **Quick Reference:** [WORKSPACE_QUICKREF.md](WORKSPACE_QUICKREF.md)
- **Visual Preview:** [WORKSPACE_VISUAL.md](WORKSPACE_VISUAL.md)
- **Project README:** [README.md](README.md)

---

## 🎉 Hoàn thành!

Bây giờ bạn đã:
- ✅ Mở workspace thành công
- ✅ Cài extensions cần thiết
- ✅ Hiểu cách navigate trong workspace
- ✅ Biết cách chạy FE và BE
- ✅ Sẵn sàng code!

**Happy Coding! 🚀**

---

### Need Help?
- 📚 Docs: Xem các file WORKSPACE_*.md
- 🐛 Issues: Tạo issue trên GitHub
- 💬 Questions: Hỏi team members

---

**Tip:** Save file này vào bookmarks để tham khảo nhanh! 📌
