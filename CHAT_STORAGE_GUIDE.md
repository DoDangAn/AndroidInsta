# 💬 Hướng Dẫn Lưu Trữ Cuộc Trò Chuyện

## 📊 Nơi Lưu Trữ Dữ Liệu Chat

### **1. DATABASE (MySQL) - PRIMARY STORAGE ✅**

**Bảng: `messages`** (Lưu trữ chính tất cả tin nhắn)

```sql
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT,
    media_url VARCHAR(255),
    message_type ENUM('text', 'image', 'video') DEFAULT 'text',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    INDEX idx_chat_history (sender_id, receiver_id, created_at),
    INDEX idx_unread (receiver_id, sender_id, is_read)
);
```

**Đặc điểm:**
- ✅ **Lưu trữ vĩnh viễn** - Dữ liệu không bao giờ bị mất
- ✅ **Có thể truy vấn** - Lấy lịch sử chat bất cứ khi nào
- ✅ **Có indexes** - Truy vấn nhanh chóng
- ✅ **Backup tự động** - MySQL backup đảm bảo an toàn dữ liệu

---

### **2. REDIS CACHE - TEMPORARY STORAGE**

**Keys sử dụng:**

```
unread:messages:{receiverId}:{senderId}
conversation:{userId}
chat:history:{userId}:{partnerId}:*
```

**Đặc điểm:**
- ⏰ **Tạm thời** - Dữ liệu chỉ lưu trong bộ nhớ
- ⚡ **Nhanh** - Giảm tải truy vấn database
- 🔄 **Tự động xóa** - Khi có tin nhắn mới, cache được invalidate
- ℹ️ **Chỉ dùng cho:**
  - Đếm tin nhắn chưa đọc (unread count)
  - Cache danh sách conversations
  - Coordination các client

---

### **3. KAFKA - MESSAGE QUEUE**

**Topics:**
```
- message-sent-events
- message-read-events
- notification-events
```

**Đặc điểm:**
- 📡 **Pub/Sub** - Phát sóng sự kiện tin nhắn
- 🔔 **Real-time** - Thông báo cho client khác
- 📝 **Event Log** - Ghi lại lịch sử sự kiện

---

## 🔄 Quy Trình Gửi Tin Nhắn

### **Step 1: Client Gửi Tin Nhắn**
```
Client (Flutter) 
  → WebSocket: /app/chat/{conversationId}
  → Backend WebSocketChatController
```

### **Step 2: Backend Xử Lý**
```kotlin
fun sendMessage(senderId, receiverId, content, messageType) {
    // 1. Lưu vào DATABASE
    val savedMessage = messageRepository.save(Message(...))
    
    // 2. Xóa REDIS CACHE (invalidate)
    redisService.delete("conversation:$senderId")
    redisService.delete("conversation:$receiverId")
    
    // 3. Gửi KAFKA EVENT
    kafkaProducerService.sendMessageSentEvent(...)
    
    // 4. Gửi qua WebSocket đến receiver
    messagingTemplate.convertAndSendToUser(
        receiverId.toString(),
        "/queue/messages",
        savedMessage
    )
}
```

### **Step 3: Receiver Nhận Tin Nhắn**
```
Backend → WebSocket 
  → Client (Flutter) nhận qua StompClient
  → UI cập nhật (FutureBuilder)
```

---

## 🗂️ Cấu Trúc Thư Mục Code

### **Backend Kotlin:**
```
spring_boot_backend/src/main/kotlin/com/androidinsta/
├── Model/
│   └── Message.kt                  ← Entity database
├── Repository/User/
│   └── MessageRepository.kt        ← Query database
├── Service/
│   ├── MessageService.kt           ← Logic xử lý
│   ├── RedisService.kt             ← Cache management
│   └── KafkaProducerService.kt     ← Event producer
└── controller/
    ├── User/
    │   ├── MessageController.kt    ← REST API
    │   └── ChatController.kt       ← Chat REST API
    └── WebSocketChatController.kt  ← WebSocket real-time
```

### **Frontend Flutter:**
```
flutter_app/lib/
├── models/
│   └── chat_models.dart            ← Message, Conversation models
├── services/
│   └── chat_service.dart           ← API calls & WebSocket
└── screens/
    ├── chat_screen.dart            ← Single chat UI
    └── chat_list_screen.dart       ← Conversations list
```

---

## 📱 Frontend: Lấy Lịch Sử Chat

### **Từ Dart/Flutter:**

```dart
// chat_service.dart
Future<List<Message>> getChatHistory(int userId, {int page = 0}) {
  // Gọi REST API từ backend
  final url = '$baseUrl/api/messages/chat/$userId?page=$page&size=20';
  final response = await http.get(url, headers: authHeaders);
  
  // Backend trả về từ DATABASE
  return Message.fromJsonList(response.body);
}

// chat_screen.dart
void initState() {
  // Lấy lịch sử chat từ database
  _chatService.getChatHistory(widget.user.id).then((messages) {
    setState(() => _messages = messages);
  });
  
  // Kết nối WebSocket để nhận tin nhắn mới real-time
  _stompClient.connect(onConnect: _subscribeToMessages);
}
```

---

## 🔐 Bảo Mật & Hiệu Năng

### **Bảo Mật:**
- ✅ JWT Authentication trên tất cả API endpoints
- ✅ Verify userId = currentUser (không cho phép xem chat của người khác)
- ✅ Encrypt media trước khi upload (Cloudinary)

### **Hiệu Năng:**
- ✅ Database indexes trên `sender_id`, `receiver_id`, `created_at`
- ✅ Pagination (20 messages/page) để giảm load
- ✅ Redis cache unread count (đỡ query database liên tục)
- ✅ WebSocket real-time thay vì polling

---

## 📊 Query Tối Ưu Hóa

```sql
-- Lấy lịch sử chat (có index)
SELECT m FROM Message m
WHERE (m.sender.id = 1 AND m.receiver.id = 2)
   OR (m.sender.id = 2 AND m.receiver.id = 1)
ORDER BY m.createdAt DESC
LIMIT 20 OFFSET 0;

-- Index giúp:
-- sender_id + receiver_id + created_at → FastPath
-- Không cần full table scan
```

---

## 🚀 Sơ Đồ Luồng Dữ Liệu

```
┌─────────────┐
│   Flutter   │ ← Client
└──────┬──────┘
       │ REST API + WebSocket
       ▼
┌──────────────────────┐
│  Spring Boot Backend │
├──────────────────────┤
│ MessageService       │
└────┬──────┬────┬─────┘
     │      │    │
     ▼      ▼    ▼
 ┌─────┐ ┌──────┐ ┌───────┐
 │MySQL│ │Redis │ │Kafka  │
 └─────┘ └──────┘ └───────┘
   Lưu     Cache  Event
  trữ     tạm    Queue
 vĩnh    thời
 viễn
```

---

## ❓ FAQ

**Q: Nếu Redis mất dữ liệu thì sao?**
- A: Không sao! Dữ liệu chính lưu ở MySQL, Redis chỉ là cache. Khi Redis start lại, cache sẽ được rebuild từ database.

**Q: Tin nhắn có được xóa không?**
- A: Hiện tại không có hard delete. Nếu muốn soft delete, thêm field `deleted_at` vào Message entity.

**Q: Có thể search lịch sử chat không?**
- A: Có! Thêm `FULLTEXT INDEX` trên `content` field để search nhanh.

**Q: Chat có end-to-end encryption không?**
- A: Hiện tại chưa có. Nên thêm nếu muốn bảo mật cao cấp.

---

**📌 Tóm tắt:** Cuộc trò chuyện được lưu **chính** ở **MySQL Database** (bảng `messages`), với Redis dùng cache tạm thời, và Kafka để broadcast events real-time.

