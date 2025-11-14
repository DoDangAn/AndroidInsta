# 📚 Workspace Documentation Index

## Mở cả Frontend và Backend cùng lúc - Tài liệu đầy đủ

Đây là hệ thống tài liệu hướng dẫn sử dụng VS Code Workspace để làm việc với cả Flutter frontend và Spring Boot backend đồng thời.

---

## 🎯 Bắt đầu ngay

### Cho người mới:
**👉 Bắt đầu tại đây:** [GETTING_STARTED.md](GETTING_STARTED.md)
- ✅ Checklist 7 bước đơn giản
- ⏱️ Hoàn thành trong 5 phút
- 🎯 Bắt đầu code ngay lập tức

### Cho người đã quen:
**👉 Quick Reference:** [WORKSPACE_QUICKREF.md](WORKSPACE_QUICKREF.md)
- ⌨️ Phím tắt quan trọng
- 🏃 Commands thường dùng
- 💡 Tips & tricks

---

## 📖 Tài liệu chi tiết

### 1. 🚀 GETTING_STARTED.md
**Ai nên đọc:** Người mới bắt đầu với workspace

**Nội dung:**
- ✅ Checklist 7 bước để setup
- 🎯 Workflow đề xuất
- ❓ Troubleshooting phổ biến
- 🛠️ Các thao tác cơ bản

**Thời gian đọc:** 5 phút

[👉 Đọc GETTING_STARTED.md](GETTING_STARTED.md)

---

### 2. 📚 WORKSPACE_GUIDE.md
**Ai nên đọc:** Mọi người trong team

**Nội dung:**
- 📂 Cấu trúc workspace chi tiết
- ⚙️ Tất cả tính năng có sẵn
- 🎯 Tasks và launch configurations
- 🐛 Hướng dẫn debug chi tiết
- 💡 Mẹo sử dụng nâng cao
- ❓ FAQ đầy đủ

**Thời gian đọc:** 15 phút

[👉 Đọc WORKSPACE_GUIDE.md](WORKSPACE_GUIDE.md)

---

### 3. 🎯 WORKSPACE_QUICKREF.md
**Ai nên đọc:** Developers đang code hàng ngày

**Nội dung:**
- ⌨️ Phím tắt (keyboard shortcuts)
- 🏃 Commands nhanh
- 🐛 Debug commands
- 🔍 Search tips
- 📋 Tasks reference

**Thời gian đọc:** 3 phút (giữ để tham khảo)

[👉 Đọc WORKSPACE_QUICKREF.md](WORKSPACE_QUICKREF.md)

---

### 4. 📸 WORKSPACE_VISUAL.md
**Ai nên đọc:** Visual learners, người muốn preview trước khi mở

**Nội dung:**
- 🎨 ASCII diagrams của UI
- 📋 Layout suggestions
- 🔍 Preview các panels
- 💡 Tips để customize UI
- ✨ Benefits của workspace

**Thời gian đọc:** 5 phút

[👉 Đọc WORKSPACE_VISUAL.md](WORKSPACE_VISUAL.md)

---

## 🗂️ File cấu hình

### AndroidInsta.code-workspace
**File chính** - Double-click để mở workspace
- 📁 Định nghĩa 3 folders (FE, BE, Root)
- ⚙️ Settings cho Dart, Kotlin, Java
- 🔌 Extensions recommendations
- 🐛 Debug configurations
- 📋 Tasks definitions

### .vscode/settings.json
**Settings chia sẻ** cho toàn team
- Format on save
- File exclusions
- Search exclusions
- Editor preferences

### .vscode/extensions.json
**Extensions đề xuất**
- Flutter & Dart
- Kotlin & Java
- Spring Boot
- Git tools
- REST client

---

## 📊 Cấu trúc thư mục

```
AndroidInsta/
├── 📄 AndroidInsta.code-workspace    ← MỞ FILE NÀY
│
├── 📚 Documentation
│   ├── GETTING_STARTED.md           ← Bắt đầu tại đây
│   ├── WORKSPACE_GUIDE.md           ← Hướng dẫn đầy đủ
│   ├── WORKSPACE_QUICKREF.md        ← Tham khảo nhanh
│   ├── WORKSPACE_VISUAL.md          ← Preview visual
│   └── WORKSPACE_INDEX.md           ← (File này)
│
├── ⚙️ Configuration
│   └── .vscode/
│       ├── settings.json            ← Shared settings
│       └── extensions.json          ← Recommended extensions
│
├── 🎨 Flutter Frontend
│   └── flutter_app/
│       ├── lib/
│       │   ├── main.dart
│       │   ├── screens/
│       │   ├── services/
│       │   └── models/
│       └── pubspec.yaml
│
└── ⚙️ Spring Boot Backend
    └── spring_boot_backend/
        ├── src/main/kotlin/
        │   └── com/androidinsta/
        │       ├── controller/
        │       ├── Model/
        │       ├── Service/
        │       └── Repository/
        └── build.gradle.kts
```

---

## 🎯 Use Cases

### Use Case 1: Onboarding developer mới
```
1. Clone repo
2. Đọc GETTING_STARTED.md
3. Mở AndroidInsta.code-workspace
4. Install extensions
5. Bắt đầu code
```

### Use Case 2: Implement feature mới
```
1. Mở workspace
2. Tham khảo WORKSPACE_GUIDE.md (phần Workflow)
3. Code backend API
4. Code frontend UI
5. Debug với "Full Stack: FE + BE"
```

### Use Case 3: Fix bug
```
1. Mở workspace
2. Search toàn bộ code (Ctrl+Shift+F)
3. Đặt breakpoints
4. Debug cả FE và BE
5. Fix và test
```

### Use Case 4: Cần giúp đỡ nhanh
```
1. Mở WORKSPACE_QUICKREF.md
2. Tìm command cần dùng
3. Execute
```

---

## 💡 Learning Path

### Level 1: Beginner (Ngày 1)
1. ✅ Đọc [GETTING_STARTED.md](GETTING_STARTED.md)
2. ✅ Mở workspace lần đầu
3. ✅ Cài extensions
4. ✅ Test chạy FE và BE

### Level 2: Intermediate (Tuần 1)
1. ✅ Đọc [WORKSPACE_GUIDE.md](WORKSPACE_GUIDE.md)
2. ✅ Thực hành các features
3. ✅ Customize settings
4. ✅ Thành thạo debug

### Level 3: Advanced (Tháng 1)
1. ✅ Master tất cả phím tắt
2. ✅ Tạo custom tasks
3. ✅ Optimize workflow
4. ✅ Share knowledge với team

---

## 🔗 Quick Links

| Tài liệu | Mô tả | Đọc ngay |
|----------|-------|----------|
| Getting Started | Bắt đầu trong 5 phút | [→](GETTING_STARTED.md) |
| Full Guide | Hướng dẫn chi tiết | [→](WORKSPACE_GUIDE.md) |
| Quick Reference | Phím tắt & commands | [→](WORKSPACE_QUICKREF.md) |
| Visual Preview | Xem giao diện | [→](WORKSPACE_VISUAL.md) |
| Main README | Project overview | [→](README.md) |

---

## ❓ Cần giúp gì?

### Tôi là người mới, bắt đầu từ đâu?
👉 [GETTING_STARTED.md](GETTING_STARTED.md)

### Tôi muốn biết tất cả features?
👉 [WORKSPACE_GUIDE.md](WORKSPACE_GUIDE.md)

### Tôi cần tham khảo nhanh phím tắt?
👉 [WORKSPACE_QUICKREF.md](WORKSPACE_QUICKREF.md)

### Tôi muốn xem workspace trông như thế nào?
👉 [WORKSPACE_VISUAL.md](WORKSPACE_VISUAL.md)

### Tôi gặp lỗi, làm sao fix?
👉 [GETTING_STARTED.md](GETTING_STARTED.md) (phần Troubleshooting)

---

## 🎓 Best Practices

1. **Luôn mở workspace** thay vì mở từng folder riêng lẻ
2. **Install tất cả recommended extensions**
3. **Sử dụng Quick Open** (Ctrl+P) thay vì click qua các folders
4. **Đặt breakpoints** ở cả FE và BE khi debug
5. **Dùng multiple terminals** cho FE và BE
6. **Commit .vscode/settings.json** để sync với team
7. **Update workspace config** khi add new features

---

## 🚀 Bắt đầu ngay!

```bash
# 1. Clone repo (nếu chưa có)
git clone https://github.com/your-repo/AndroidInsta.git
cd AndroidInsta

# 2. Mở workspace
code AndroidInsta.code-workspace

# 3. Đọc getting started
# Mở file GETTING_STARTED.md trong VS Code

# 4. Bắt đầu code! 🎉
```

---

**Happy Coding với AndroidInsta Workspace! 🎨⚙️**

---

*Cập nhật lần cuối: November 2025*
