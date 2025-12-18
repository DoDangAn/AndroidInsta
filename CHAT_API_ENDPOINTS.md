# 📞 API Endpoints - Lịch Sử Chat

## 🔗 Endpoint Lấy Lịch Sử Chat

### **1. REST API - GET Chat History (Chính)**

#### **Endpoint 1: `/api/chat/{userId}`** ⭐ (CHỈ NHO)
```
GET http://localhost:8081/api/chat/{userId}?page=0&size=50
```

**Backend Controller:**
```kotlin
// File: ChatController.kt
@RestController
@RequestMapping("/api/chat")
class ChatController {
    
    @GetMapping("/{userId}")  // ← Endpoint này
    fun getChatHistory(
        @PathVariable userId: Long,           // ID của người chat với
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ChatHistoryResponse> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        
        // Gọi service lấy dữ liệu từ DATABASE
        val messages = messageService.getChatHistory(currentUserId, userId, pageable)
        
        // Đánh dấu tin nhắn là đã đọc
        messageService.markAsRead(currentUserId, userId)
        
        return ResponseEntity.ok(
            ChatHistoryResponse(
                messages = messages.content.map { it.toDto() }.reversed(),
                currentPage = messages.number,
                totalPages = messages.totalPages,
                totalMessages = messages.totalElements
            )
        )
    }
}
```

**Dòng gọi:** `messageService.getChatHistory(currentUserId, userId, pageable)`
- **File:** `MessageService.kt`
- **Method:** `getChatHistory(userId, partnerId, pageable)`
- **Dữ liệu lấy từ:** MySQL Database (Bảng `messages`)

---

#### **Endpoint 2: `/api/messages/chat/{partnerId}`** (CỔ)
```
GET http://localhost:8081/api/messages/chat/{partnerId}?page=0&size=50
```

**Backend Controller:**
```kotlin
// File: MessageController.kt
@RestController
@RequestMapping("/api/messages")
class MessageController {
    
    @GetMapping("/chat/{partnerId}")  // ← Endpoint này (cũ)
    fun getChatHistory(
        @PathVariable partnerId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<MessagesResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        
        // Gọi service lấy dữ liệu từ DATABASE
        val messagesPage = messageService.getChatHistory(userId, partnerId, pageable)
        
        val messageDtos = messagesPage.content.map { it.toDto() }
        return ResponseEntity.ok(...)
    }
}
```

**⚠️ Lưu ý:** Endpoint này cũ rồi, nên dùng `/api/chat/{userId}` thay vào.

---

### **2. Frontend - Gọi API Từ Flutter**

**File: `chat_service.dart`**
```dart
class ChatService {
  final String baseUrl = ApiConfig.baseUrl;  // http://10.0.2.2:8081
  
  /// Lấy chat history với một user
  Future<ChatHistory> getChatHistory(int userId, {int page = 0, int size = 50}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    // ← GỌI ĐẾN ENDPOINT
    final response = await http.get(
      Uri.parse('$baseUrl/api/messages/chat/$userId?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      if (data['success'] == true) {
        return ChatHistory.fromJson(data['data']);
      } else {
        throw Exception(data['message'] ?? 'Failed to load chat history');
      }
    } else {
      throw Exception('Failed to load chat history');
    }
  }
}
```

**Nơi gọi trong UI:**
```dart
// File: chat_screen.dart
class ChatScreen extends StatefulWidget {
  final User user;  // Người chat cùng
  
  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final ChatService _chatService = ChatService();
  List<ChatMessage> _messages = [];
  
  @override
  void initState() {
    super.initState();
    _loadChatHistory();
  }
  
  /// ← ĐÂY LÀ NƠI GỌI
  Future<void> _loadChatHistory() async {
    try {
      final history = await _chatService.getChatHistory(
        widget.user.id,  // ID của người chat cùng
        page: 0,
        size: 50
      );
      
      setState(() {
        _messages = history.messages;
      });
      
      // Kết nối WebSocket để nhận tin nhắn mới real-time
      _connectWebSocket();
    } catch (e) {
      print('Error loading chat history: $e');
    }
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.user.fullName)),
      body: _messages.isEmpty 
        ? Center(child: CircularProgressIndicator())
        : ListView.builder(
            itemCount: _messages.length,
            itemBuilder: (context, index) {
              final message = _messages[index];
              return MessageBubble(message: message);
            },
          ),
    );
  }
}
```

---

### **3. Backend - Truy Vấn Database**

**File: `MessageRepository.kt`**
```kotlin
@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    
    /// ← ĐÂY LÀ QUERY LẤY LỊCH SỬ CHAT
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
           OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
        ORDER BY m.createdAt DESC
    """)
    fun findChatHistory(
        @Param("userId1") userId1: Long,
        @Param("userId2") userId2: Long,
        pageable: Pageable
    ): Page<Message>
}
```

**Truy vấn SQL thực tế:**
```sql
SELECT * FROM messages
WHERE (sender_id = 1 AND receiver_id = 2)
   OR (sender_id = 2 AND receiver_id = 1)
ORDER BY created_at DESC
LIMIT 50 OFFSET 0;

-- Indexes giúp nhanh:
-- INDEX idx_chat_history (sender_id, receiver_id, created_at)
```

**File: `MessageService.kt`**
```kotlin
@Service
class MessageService {
    
    /// ← GỌIDẾN REPOSITORY LẤY DỮ LIỆU
    fun getChatHistory(userId: Long, partnerId: Long, pageable: Pageable): Page<Message> {
        return messageRepository.findChatHistory(userId, partnerId, pageable)
    }
}
```

---

## 🔄 Luồng Gọi Chi Tiết

```
┌─────────────────────┐
│   chat_screen.dart  │
│  (Flutter UI)       │
└──────────┬──────────┘
           │ 1. Gọi getChatHistory(userId)
           ▼
┌─────────────────────────┐
│   chat_service.dart     │
│  (ChatService class)    │
└──────────┬──────────────┘
           │ 2. HTTP GET /api/messages/chat/{userId}
           ▼
┌──────────────────────────────────┐
│    Backend (Spring Boot)         │
│ ChatController.kt                │
│ /api/chat/{userId}               │
└──────────┬───────────────────────┘
           │ 3. Gọi getChatHistory()
           ▼
┌──────────────────────────────────┐
│    MessageService.kt             │
│ getChatHistory(userId, partnerId)│
└──────────┬───────────────────────┘
           │ 4. Gọi findChatHistory()
           ▼
┌──────────────────────────────────┐
│    MessageRepository.kt          │
│ findChatHistory() - Query DB     │
└──────────┬───────────────────────┘
           │ 5. SELECT * FROM messages WHERE...
           ▼
┌──────────────────────────────────┐
│    MySQL Database                │
│    Bảng: messages                │
└──────────┬───────────────────────┘
           │ 6. Trả về danh sách Message
           ▼
┌──────────────────────────────────┐
│    Response JSON                 │
│ {                                │
│   "messages": [                  │
│     { "id": 1, "sender": {...} } │
│   ]                              │
│ }                                │
└─────────────────────────────────┘
```

---

## 📝 Request/Response Example

### **Request:**
```bash
GET http://localhost:8081/api/messages/chat/123?page=0&size=50
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

### **Response:**
```json
{
  "success": true,
  "message": "Chat history retrieved successfully",
  "data": {
    "messages": [
      {
        "id": 1001,
        "senderId": 1,
        "senderName": "User A",
        "receiverId": 123,
        "content": "Hello!",
        "messageType": "text",
        "isRead": true,
        "createdAt": "2025-12-11T10:30:00"
      },
      {
        "id": 1000,
        "senderId": 123,
        "senderName": "User B",
        "receiverId": 1,
        "content": "Hi there!",
        "messageType": "text",
        "isRead": true,
        "createdAt": "2025-12-11T10:25:00"
      }
    ],
    "currentPage": 0,
    "totalPages": 5,
    "totalMessages": 250
  }
}
```

---

## ✅ Tóm Tắt

| Thành Phần | Vị Trí | Mục Đích |
|-----------|--------|---------|
| **UI** | `chat_screen.dart` | Hiển thị tin nhắn, gọi service khi initState |
| **Service** | `chat_service.dart` | HTTP GET → Backend |
| **Controller** | `ChatController.kt` | Nhận request, gọi service |
| **Service** | `MessageService.kt` | Xử lý logic, gọi repository |
| **Repository** | `MessageRepository.kt` | Viết query, lấy dữ liệu từ DB |
| **Database** | MySQL `messages` table | Lưu trữ tất cả tin nhắn |

---

## 🚀 Endpoints Liên Quan

```
GET  /api/chat/conversations              ← Danh sách cuộc trò chuyện
GET  /api/chat/{userId}                   ← Lịch sử chat (CHỈ NHO)
GET  /api/messages/chat/{partnerId}       ← Lịch sử chat (cũ)
POST /api/chat/send                       ← Gửi tin nhắn
GET  /api/messages/unread/{partnerId}     ← Đếm tin nhắn chưa đọc
```

