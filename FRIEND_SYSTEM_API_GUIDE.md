# Friend System API - AndroidInsta

## Tổng quan
Hệ thống kết bạn hoàn chỉnh với friend requests và quản lý friendship. **Chỉ những người bạn bè mới nhận notifications** (POST, LIKE, COMMENT, REPLY).

## Database Models

### FriendRequest
Lưu trữ lời mời kết bạn:
```kotlin
data class FriendRequest(
    val id: Long,
    val sender: User,        // Người gửi lời mời
    val receiver: User,      // Người nhận lời mời
    val status: FriendRequestStatus, // PENDING/ACCEPTED/REJECTED/CANCELLED
    val createdAt: LocalDateTime,
    val respondedAt: LocalDateTime?
)
```

### Friendship
Lưu trữ quan hệ bạn bè (bidirectional):
```kotlin
data class Friendship(
    val id: Long,
    val user: User,
    val friend: User,
    val createdAt: LocalDateTime
)
```

## API Endpoints

### 1. Gửi Friend Request
```http
POST /api/friends/requests
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "receiverId": 123
}
```

**Response:**
```json
{
  "id": 456,
  "senderId": 100,
  "senderUsername": "john_doe",
  "senderFullName": "John Doe",
  "senderAvatarUrl": "https://...",
  "receiverId": 123,
  "receiverUsername": "jane_smith",
  "receiverFullName": "Jane Smith",
  "receiverAvatarUrl": "https://...",
  "status": "PENDING",
  "createdAt": "2025-11-04T10:30:00",
  "respondedAt": null
}
```

**Notification:** Receiver nhận FRIEND_REQUEST notification

### 2. Lấy Friend Requests Đã Nhận
```http
GET /api/friends/requests/received?page=0&size=20
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 456,
      "senderId": 100,
      "senderUsername": "john_doe",
      "senderFullName": "John Doe",
      "senderAvatarUrl": "https://...",
      "status": "PENDING",
      "createdAt": "2025-11-04T10:30:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### 3. Lấy Friend Requests Đã Gửi
```http
GET /api/friends/requests/sent?page=0&size=20
Authorization: Bearer {access_token}
```

### 4. Đếm Pending Requests
```http
GET /api/friends/requests/count
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "count": 5
}
```

### 5. Chấp Nhận Friend Request
```http
PUT /api/friends/requests/{requestId}/accept
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "id": 456,
  "status": "ACCEPTED",
  "respondedAt": "2025-11-04T10:35:00"
}
```

**Actions:**
- Tạo 2 Friendship records (bidirectional)
- Sender nhận FRIEND_ACCEPT notification

### 6. Từ Chối Friend Request
```http
PUT /api/friends/requests/{requestId}/reject
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "id": 456,
  "status": "REJECTED",
  "respondedAt": "2025-11-04T10:35:00"
}
```

### 7. Hủy Friend Request Đã Gửi
```http
DELETE /api/friends/requests/{requestId}
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "message": "Friend request cancelled"
}
```

### 8. Lấy Danh Sách Bạn Bè
```http
GET /api/friends?page=0&size=20
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 789,
      "userId": 123,
      "username": "jane_smith",
      "fullName": "Jane Smith",
      "avatarUrl": "https://...",
      "bio": "Hello world",
      "mutualFriendsCount": 15,
      "friendsSince": "2025-10-01T08:00:00"
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

### 9. Lấy Bạn Bè Của User Khác
```http
GET /api/friends/{userId}?page=0&size=20
Authorization: Bearer {access_token}
```

### 10. Đếm Số Bạn Bè
```http
GET /api/friends/count
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "count": 42
}
```

### 11. Đếm Số Bạn Bè Của User Khác
```http
GET /api/friends/{userId}/count
Authorization: Bearer {access_token}
```

### 12. Unfriend (Xóa Bạn)
```http
DELETE /api/friends/{friendId}
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "message": "Unfriended successfully"
}
```

### 13. Lấy Mutual Friends (Bạn Chung)
```http
GET /api/friends/mutual/{userId}?page=0&size=20
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "content": [
    {
      "userId": 200,
      "username": "alice",
      "fullName": "Alice Brown",
      "mutualFriendsCount": 8,
      "friendsSince": "2025-09-15T12:00:00"
    }
  ]
}
```

### 14. Lấy Friend Suggestions (Gợi ý kết bạn)
```http
GET /api/friends/suggestions?page=0&size=10
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "content": [
    {
      "userId": 300,
      "username": "bob",
      "fullName": "Bob Wilson",
      "avatarUrl": "https://...",
      "bio": "Developer",
      "mutualFriendsCount": 12
    }
  ]
}
```

**Logic:** Gợi ý là bạn của bạn bè (chưa kết bạn, chưa gửi request)

### 15. Kiểm Tra Friendship Status
```http
GET /api/friends/status/{userId}
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "isFriend": false,
  "hasPendingRequest": true,
  "pendingRequestSentByMe": true,
  "friendRequestId": 456
}
```

## Notification System

### Notifications Chỉ Gửi Cho Bạn Bè

#### POST Notification
Khi user A đăng bài → tất cả **bạn bè** của A nhận notification:
```json
{
  "type": "POST",
  "message": "john_doe đã đăng bài viết mới",
  "entityId": 789
}
```

#### LIKE Notification
Khi user A like post của user B → nếu A và B là **bạn bè** → B nhận notification:
```json
{
  "type": "LIKE",
  "message": "john_doe đã thích bài viết của bạn",
  "entityId": 789
}
```

#### COMMENT Notification
Khi user A comment vào post của user B → nếu A và B là **bạn bè** → B nhận notification:
```json
{
  "type": "COMMENT",
  "message": "john_doe đã bình luận về bài viết của bạn: 'Great post!'",
  "entityId": 789
}
```

#### REPLY Notification
Khi user A reply comment của user B → nếu A và B là **bạn bè** → B nhận notification:
```json
{
  "type": "REPLY",
  "message": "john_doe đã trả lời bình luận của bạn: 'I agree!'",
  "entityId": 789
}
```

#### FRIEND_REQUEST Notification
Khi user A gửi friend request cho user B → B nhận notification:
```json
{
  "type": "FRIEND_REQUEST",
  "message": "john_doe đã gửi lời mời kết bạn"
}
```

#### FRIEND_ACCEPT Notification
Khi user B chấp nhận friend request của user A → A nhận notification:
```json
{
  "type": "FRIEND_ACCEPT",
  "message": "jane_smith đã chấp nhận lời mời kết bạn của bạn"
}
```

## Flutter Integration

### 1. Models
```dart
class FriendRequest {
  final int id;
  final int senderId;
  final String senderUsername;
  final String? senderFullName;
  final String? senderAvatarUrl;
  final int receiverId;
  final String receiverUsername;
  final String? receiverFullName;
  final String? receiverAvatarUrl;
  final String status; // PENDING, ACCEPTED, REJECTED, CANCELLED
  final DateTime createdAt;
  final DateTime? respondedAt;

  FriendRequest({
    required this.id,
    required this.senderId,
    required this.senderUsername,
    this.senderFullName,
    this.senderAvatarUrl,
    required this.receiverId,
    required this.receiverUsername,
    this.receiverFullName,
    this.receiverAvatarUrl,
    required this.status,
    required this.createdAt,
    this.respondedAt,
  });

  factory FriendRequest.fromJson(Map<String, dynamic> json) {
    return FriendRequest(
      id: json['id'],
      senderId: json['senderId'],
      senderUsername: json['senderUsername'],
      senderFullName: json['senderFullName'],
      senderAvatarUrl: json['senderAvatarUrl'],
      receiverId: json['receiverId'],
      receiverUsername: json['receiverUsername'],
      receiverFullName: json['receiverFullName'],
      receiverAvatarUrl: json['receiverAvatarUrl'],
      status: json['status'],
      createdAt: DateTime.parse(json['createdAt']),
      respondedAt: json['respondedAt'] != null 
          ? DateTime.parse(json['respondedAt']) 
          : null,
    );
  }
}

class Friend {
  final int id;
  final int userId;
  final String username;
  final String? fullName;
  final String? avatarUrl;
  final String? bio;
  final int mutualFriendsCount;
  final DateTime friendsSince;

  Friend({
    required this.id,
    required this.userId,
    required this.username,
    this.fullName,
    this.avatarUrl,
    this.bio,
    required this.mutualFriendsCount,
    required this.friendsSince,
  });

  factory Friend.fromJson(Map<String, dynamic> json) {
    return Friend(
      id: json['id'],
      userId: json['userId'],
      username: json['username'],
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
      bio: json['bio'],
      mutualFriendsCount: json['mutualFriendsCount'],
      friendsSince: DateTime.parse(json['friendsSince']),
    );
  }
}

class FriendshipStatus {
  final bool isFriend;
  final bool hasPendingRequest;
  final bool pendingRequestSentByMe;
  final int? friendRequestId;

  FriendshipStatus({
    required this.isFriend,
    required this.hasPendingRequest,
    required this.pendingRequestSentByMe,
    this.friendRequestId,
  });

  factory FriendshipStatus.fromJson(Map<String, dynamic> json) {
    return FriendshipStatus(
      isFriend: json['isFriend'],
      hasPendingRequest: json['hasPendingRequest'],
      pendingRequestSentByMe: json['pendingRequestSentByMe'],
      friendRequestId: json['friendRequestId'],
    );
  }
}
```

### 2. Friend Service
```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

class FriendService {
  final String baseUrl = 'http://10.0.2.2:8081/api/friends';

  // Gửi friend request
  Future<FriendRequest> sendFriendRequest(
    String accessToken,
    int receiverId,
  ) async {
    final response = await http.post(
      Uri.parse('$baseUrl/requests'),
      headers: {
        'Authorization': 'Bearer $accessToken',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'receiverId': receiverId}),
    );

    if (response.statusCode == 201) {
      return FriendRequest.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to send friend request');
  }

  // Lấy friend requests đã nhận
  Future<List<FriendRequest>> getReceivedRequests(
    String accessToken, {
    int page = 0,
    int size = 20,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/requests/received?page=$page&size=$size'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return (data['content'] as List)
          .map((json) => FriendRequest.fromJson(json))
          .toList();
    }
    throw Exception('Failed to load friend requests');
  }

  // Chấp nhận friend request
  Future<FriendRequest> acceptFriendRequest(
    String accessToken,
    int requestId,
  ) async {
    final response = await http.put(
      Uri.parse('$baseUrl/requests/$requestId/accept'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      return FriendRequest.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to accept friend request');
  }

  // Từ chối friend request
  Future<FriendRequest> rejectFriendRequest(
    String accessToken,
    int requestId,
  ) async {
    final response = await http.put(
      Uri.parse('$baseUrl/requests/$requestId/reject'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      return FriendRequest.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to reject friend request');
  }

  // Hủy friend request
  Future<void> cancelFriendRequest(
    String accessToken,
    int requestId,
  ) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/requests/$requestId'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to cancel friend request');
    }
  }

  // Lấy danh sách bạn bè
  Future<List<Friend>> getFriends(
    String accessToken, {
    int page = 0,
    int size = 20,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl?page=$page&size=$size'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return (data['content'] as List)
          .map((json) => Friend.fromJson(json))
          .toList();
    }
    throw Exception('Failed to load friends');
  }

  // Unfriend
  Future<void> unfriend(String accessToken, int friendId) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/$friendId'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to unfriend');
    }
  }

  // Kiểm tra friendship status
  Future<FriendshipStatus> getFriendshipStatus(
    String accessToken,
    int userId,
  ) async {
    final response = await http.get(
      Uri.parse('$baseUrl/status/$userId'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      return FriendshipStatus.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to get friendship status');
  }

  // Lấy friend suggestions
  Future<List<Friend>> getFriendSuggestions(
    String accessToken, {
    int page = 0,
    int size = 10,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/suggestions?page=$page&size=$size'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return (data['content'] as List)
          .map((json) => Friend.fromJson(json))
          .toList();
    }
    throw Exception('Failed to load friend suggestions');
  }

  // Đếm pending requests
  Future<int> getPendingRequestsCount(String accessToken) async {
    final response = await http.get(
      Uri.parse('$baseUrl/requests/count'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['count'];
    }
    throw Exception('Failed to get pending requests count');
  }
}
```

### 3. Friend Requests Screen
```dart
class FriendRequestsScreen extends StatefulWidget {
  @override
  _FriendRequestsScreenState createState() => _FriendRequestsScreenState();
}

class _FriendRequestsScreenState extends State<FriendRequestsScreen> {
  final FriendService _friendService = FriendService();
  List<FriendRequest> _requests = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadRequests();
  }

  Future<void> _loadRequests() async {
    setState(() => _isLoading = true);
    try {
      final token = await _getAccessToken();
      final requests = await _friendService.getReceivedRequests(token);
      setState(() {
        _requests = requests;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _showError('Failed to load friend requests');
    }
  }

  Future<void> _acceptRequest(int requestId) async {
    try {
      final token = await _getAccessToken();
      await _friendService.acceptFriendRequest(token, requestId);
      _loadRequests(); // Reload
      _showSuccess('Friend request accepted!');
    } catch (e) {
      _showError('Failed to accept request');
    }
  }

  Future<void> _rejectRequest(int requestId) async {
    try {
      final token = await _getAccessToken();
      await _friendService.rejectFriendRequest(token, requestId);
      _loadRequests(); // Reload
      _showSuccess('Friend request rejected');
    } catch (e) {
      _showError('Failed to reject request');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Friend Requests'),
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : _requests.isEmpty
              ? Center(child: Text('No friend requests'))
              : ListView.builder(
                  itemCount: _requests.length,
                  itemBuilder: (context, index) {
                    final request = _requests[index];
                    return ListTile(
                      leading: CircleAvatar(
                        backgroundImage: request.senderAvatarUrl != null
                            ? NetworkImage(request.senderAvatarUrl!)
                            : null,
                        child: request.senderAvatarUrl == null
                            ? Icon(Icons.person)
                            : null,
                      ),
                      title: Text(request.senderUsername),
                      subtitle: Text(request.senderFullName ?? ''),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          IconButton(
                            icon: Icon(Icons.check, color: Colors.green),
                            onPressed: () => _acceptRequest(request.id),
                          ),
                          IconButton(
                            icon: Icon(Icons.close, color: Colors.red),
                            onPressed: () => _rejectRequest(request.id),
                          ),
                        ],
                      ),
                    );
                  },
                ),
    );
  }
}
```

### 4. Profile Screen với Friend Button
```dart
class UserProfileScreen extends StatefulWidget {
  final int userId;

  UserProfileScreen({required this.userId});

  @override
  _UserProfileScreenState createState() => _UserProfileScreenState();
}

class _UserProfileScreenState extends State<UserProfileScreen> {
  final FriendService _friendService = FriendService();
  FriendshipStatus? _friendshipStatus;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadFriendshipStatus();
  }

  Future<void> _loadFriendshipStatus() async {
    setState(() => _isLoading = true);
    try {
      final token = await _getAccessToken();
      final status = await _friendService.getFriendshipStatus(token, widget.userId);
      setState(() {
        _friendshipStatus = status;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _sendFriendRequest() async {
    try {
      final token = await _getAccessToken();
      await _friendService.sendFriendRequest(token, widget.userId);
      _loadFriendshipStatus();
      _showSuccess('Friend request sent!');
    } catch (e) {
      _showError('Failed to send friend request');
    }
  }

  Future<void> _cancelFriendRequest() async {
    if (_friendshipStatus?.friendRequestId != null) {
      try {
        final token = await _getAccessToken();
        await _friendService.cancelFriendRequest(
          token,
          _friendshipStatus!.friendRequestId!,
        );
        _loadFriendshipStatus();
        _showSuccess('Friend request cancelled');
      } catch (e) {
        _showError('Failed to cancel friend request');
      }
    }
  }

  Future<void> _unfriend() async {
    try {
      final token = await _getAccessToken();
      await _friendService.unfriend(token, widget.userId);
      _loadFriendshipStatus();
      _showSuccess('Unfriended');
    } catch (e) {
      _showError('Failed to unfriend');
    }
  }

  Widget _buildFriendButton() {
    if (_isLoading || _friendshipStatus == null) {
      return CircularProgressIndicator();
    }

    if (_friendshipStatus!.isFriend) {
      return ElevatedButton.icon(
        onPressed: _unfriend,
        icon: Icon(Icons.person_remove),
        label: Text('Unfriend'),
        style: ElevatedButton.styleFrom(backgroundColor: Colors.grey),
      );
    }

    if (_friendshipStatus!.hasPendingRequest) {
      if (_friendshipStatus!.pendingRequestSentByMe) {
        return ElevatedButton.icon(
          onPressed: _cancelFriendRequest,
          icon: Icon(Icons.cancel),
          label: Text('Cancel Request'),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.orange),
        );
      } else {
        return ElevatedButton.icon(
          onPressed: () {
            // Navigate to friend requests screen
            Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => FriendRequestsScreen()),
            );
          },
          icon: Icon(Icons.person_add),
          label: Text('Respond'),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.blue),
        );
      }
    }

    return ElevatedButton.icon(
      onPressed: _sendFriendRequest,
      icon: Icon(Icons.person_add),
      label: Text('Add Friend'),
      style: ElevatedButton.styleFrom(backgroundColor: Colors.blue),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Profile')),
      body: Column(
        children: [
          // Profile info...
          SizedBox(height: 16),
          _buildFriendButton(),
          // Rest of profile...
        ],
      ),
    );
  }
}
```

## Testing

### Test gửi friend request
```bash
curl -X POST http://localhost:8081/api/friends/requests \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"receiverId": 2}'
```

### Test chấp nhận friend request
```bash
curl -X PUT http://localhost:8081/api/friends/requests/1/accept \
  -H "Authorization: Bearer {token}"
```

### Test lấy danh sách bạn bè
```bash
curl -X GET http://localhost:8081/api/friends \
  -H "Authorization: Bearer {token}"
```

### Test unfriend
```bash
curl -X DELETE http://localhost:8081/api/friends/2 \
  -H "Authorization: Bearer {token}"
```

## Database Migration

```sql
-- Friend Requests Table
CREATE TABLE friend_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    responded_at DATETIME,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    UNIQUE KEY unique_request (sender_id, receiver_id),
    INDEX idx_receiver_status (receiver_id, status),
    INDEX idx_sender_status (sender_id, status)
);

-- Friendships Table
CREATE TABLE friendships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_friendship (user_id, friend_id),
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id)
);
```

## Key Features

✅ **Friend Request System**
- Gửi/chấp nhận/từ chối/hủy friend requests
- Unique constraint: không duplicate requests

✅ **Bidirectional Friendship**
- Khi chấp nhận request → tạo 2 Friendship records
- Unfriend → xóa cả 2 records

✅ **Notifications Cho Bạn Bè**
- POST: chỉ bạn bè nhận notification
- LIKE/COMMENT/REPLY: kiểm tra areFriends() trước khi gửi
- FRIEND_REQUEST/FRIEND_ACCEPT: notifications cho friend system

✅ **Friend Suggestions**
- Gợi ý: bạn của bạn bè (chưa kết bạn)
- Loại trừ: đã là bạn, đã gửi/nhận pending request

✅ **Mutual Friends**
- Hiển thị số bạn chung
- Query hiệu quả với JOIN

## Best Practices

1. **Privacy**: Chỉ bạn bè mới nhận post notifications
2. **Bidirectional**: Friendship luôn 2 chiều (user ↔ friend)
3. **Unique Constraints**: Tránh duplicate requests/friendships
4. **Indexing**: Index trên receiver_id, sender_id, status
5. **Cascade Delete**: Xóa user → xóa tất cả friendships/requests
