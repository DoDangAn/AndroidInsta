# Notification System - AndroidInsta

## Tổng quan
Hệ thống notification real-time sử dụng Kafka + WebSocket để gửi thông báo tức thời đến người dùng.

## Các loại Notification

```kotlin
enum class NotificationType {
    LIKE,           // Ai đó thích bài viết của bạn
    COMMENT,        // Ai đó comment vào bài viết của bạn
    FOLLOW,         // Ai đó follow bạn
    MESSAGE,        // Ai đó gửi tin nhắn cho bạn
    POST,           // Bạn bè đăng bài viết mới
    REPLY,          // Ai đó trả lời tin nhắn của bạn
    MENTION         // Ai đó mention bạn
}
```

## API Endpoints

### 1. Lấy danh sách notifications
```http
GET /api/notifications?page=0&size=20
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "senderId": 123,
      "senderUsername": "john_doe",
      "senderAvatarUrl": "https://...",
      "type": "LIKE",
      "message": "john_doe đã thích bài viết của bạn",
      "entityId": 456,
      "isRead": false,
      "createdAt": "2025-11-04T10:30:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 3
}
```

### 2. Lấy unread notifications
```http
GET /api/notifications/unread?page=0&size=20
Authorization: Bearer {access_token}
```

### 3. Đếm số unread notifications
```http
GET /api/notifications/unread/count
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "count": 15
}
```

### 4. Đánh dấu notification đã đọc
```http
PUT /api/notifications/{id}/read
Authorization: Bearer {access_token}
```

### 5. Đánh dấu tất cả đã đọc
```http
PUT /api/notifications/read-all
Authorization: Bearer {access_token}
```

### 6. Xóa notification
```http
DELETE /api/notifications/{id}
Authorization: Bearer {access_token}
```

## WebSocket Real-time

### Kết nối WebSocket
```javascript
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': 'Bearer ' + accessToken
}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to personal notification queue
    stompClient.subscribe('/user/queue/notifications', function(notification) {
        const notif = JSON.parse(notification.body);
        console.log('New notification:', notif);
        
        // Hiển thị notification trong UI
        showNotification(notif);
    });
});
```

### Flutter WebSocket
```dart
import 'package:stomp_dart_client/stomp_dart_client.dart';

class NotificationService {
  StompClient? _stompClient;
  
  void connect(String accessToken, int userId) {
    _stompClient = StompClient(
      config: StompConfig.SockJS(
        url: 'http://10.0.2.2:8081/ws',
        onConnect: (StompFrame frame) {
          print('Connected to WebSocket');
          
          // Subscribe to notifications
          _stompClient!.subscribe(
            destination: '/user/queue/notifications',
            headers: {'Authorization': 'Bearer $accessToken'},
            callback: (StompFrame frame) {
              if (frame.body != null) {
                final notification = jsonDecode(frame.body!);
                _handleNotification(notification);
              }
            },
          );
        },
        onWebSocketError: (dynamic error) {
          print('WebSocket Error: $error');
        },
      ),
    );
    
    _stompClient!.activate();
  }
  
  void _handleNotification(Map<String, dynamic> notification) {
    // Hiển thị notification
    print('New notification: ${notification['message']}');
    
    // Có thể show local notification
    // hoặc update UI badge count
  }
  
  void disconnect() {
    _stompClient?.deactivate();
  }
}
```

## Tự động gửi Notifications

### 1. Khi có người like bài viết
```kotlin
// Trong LikeService.likePost()
if (post.user.id != userId) {
    notificationService.sendNotification(
        receiverId = post.user.id,
        senderId = userId,
        type = NotificationType.LIKE,
        entityId = postId,
        message = "${user.username} đã thích bài viết của bạn"
    )
}
```

### 2. Khi có người comment
```kotlin
// Trong CommentService.addComment()
if (post.user.id != userId) {
    notificationService.sendNotification(
        receiverId = post.user.id,
        senderId = userId,
        type = NotificationType.COMMENT,
        entityId = postId,
        message = "${user.username} đã bình luận về bài viết của bạn"
    )
}
```

### 3. Khi có người follow
```kotlin
// Trong FollowService.followUser()
notificationService.sendNotification(
    receiverId = followedId,
    senderId = followerId,
    type = NotificationType.FOLLOW,
    entityId = null,
    message = "${follower.username} đã bắt đầu theo dõi bạn"
)
```

### 4. Khi bạn bè đăng bài mới
```kotlin
// Trong PostService.createPost()
user.followers.forEach { follow ->
    notificationService.sendNotification(
        receiverId = follow.follower.id,
        senderId = userId,
        type = NotificationType.POST,
        entityId = savedPost.id,
        message = "${user.username} đã đăng bài viết mới"
    )
}
```

## Kafka Topics

- **Topic:** `notification-events`
- **Consumer Group:** `notification-group`
- **Message Format:**
```json
{
  "receiverId": 123,
  "senderId": 456,
  "type": "LIKE",
  "entityId": 789,
  "message": "john_doe đã thích bài viết của bạn"
}
```

## Database Schema

```sql
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    INDEX idx_receiver_created (receiver_id, created_at DESC),
    INDEX idx_receiver_read (receiver_id, is_read)
);
```

## Clean up task (optional)

Tự động xóa notifications cũ hơn 30 ngày:

```kotlin
@Scheduled(cron = "0 0 2 * * ?") // Chạy lúc 2h sáng mỗi ngày
fun cleanupOldNotifications() {
    notificationService.deleteOldNotifications()
}
```

## Testing

### Test gửi notification
```bash
curl -X POST http://localhost:8081/api/posts/{postId}/like \
  -H "Authorization: Bearer {token}"
```

Kiểm tra:
1. Notification được lưu vào database
2. Real-time notification qua WebSocket
3. Unread count tăng lên

### Test WebSocket
```javascript
// Mở browser console và test
const socket = new SockJS('http://localhost:8081/ws');
const client = Stomp.over(socket);
client.connect({'Authorization': 'Bearer YOUR_TOKEN'}, () => {
    client.subscribe('/user/queue/notifications', (msg) => {
        console.log('Received:', JSON.parse(msg.body));
    });
});
```

## Troubleshooting

### Notification không nhận được
1. Kiểm tra Kafka đang chạy: `docker ps | grep kafka`
2. Kiểm tra WebSocket connection
3. Kiểm tra token có đúng không
4. Check logs: `docker logs androidinsta-backend`

### Duplicate notifications
- Kiểm tra Kafka consumer group config
- Đảm bảo không có multiple instances consume cùng 1 topic

## Best Practices

1. **Batch notifications:** Không gửi quá nhiều notifications trong thời gian ngắn
2. **Rate limiting:** Giới hạn số notifications per user per hour
3. **Cleanup:** Tự động xóa notifications cũ
4. **Pagination:** Luôn sử dụng pagination khi fetch notifications
5. **Read status:** Update read status khi user xem notification
