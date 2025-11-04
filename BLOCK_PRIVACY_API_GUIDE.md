# Block Users & Private Account API - AndroidInsta

## Tổng quan
Hệ thống Block Users và Private Account để bảo vệ privacy và kiểm soát người dùng.

## Database Models

### Block
Lưu trữ quan hệ chặn người dùng:
```kotlin
data class Block(
    val id: Long,
    val blocker: User,    // Người chặn
    val blocked: User,    // Người bị chặn
    val createdAt: LocalDateTime
)
```

### User (Updated)
Thêm field `isPrivate`:
```kotlin
data class User(
    // ... existing fields
    val isPrivate: Boolean = false,  // NEW: Private account setting
    // ...
)
```

## Block User API

### 1. Block User
```http
POST /api/blocks
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "blockedUserId": 123
}
```

**Response:**
```json
{
  "blockId": 456,
  "userId": 123,
  "username": "john_doe",
  "fullName": "John Doe",
  "avatarUrl": "https://...",
  "blockedAt": "2025-11-04T10:30:00"
}
```

**Actions khi block:**
- Xóa friendship (cả 2 chiều) nếu có
- Xóa follow relationships (cả 2 chiều)
- Không thể xem posts/profile của nhau
- Không thể gửi messages

### 2. Unblock User
```http
DELETE /api/blocks/{blockedUserId}
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "message": "User unblocked successfully"
}
```

### 3. Lấy Danh Sách Blocked Users
```http
GET /api/blocks?page=0&size=20
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "content": [
    {
      "blockId": 456,
      "userId": 123,
      "username": "john_doe",
      "fullName": "John Doe",
      "avatarUrl": "https://...",
      "blockedAt": "2025-11-04T10:30:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### 4. Đếm Blocked Users
```http
GET /api/blocks/count
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "count": 5
}
```

### 5. Kiểm Tra User Đã Block
```http
GET /api/blocks/check/{userId}
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "isBlocked": true
}
```

### 6. Kiểm Tra Block Relationship
```http
GET /api/blocks/has-block/{userId}
Authorization: Bearer {access_token}
```

Kiểm tra có block relationship giữa 2 users không (bất kỳ chiều nào)

**Response:**
```json
{
  "hasBlock": true
}
```

### 7. Kiểm Tra Quyền Truy Cập Account
```http
GET /api/blocks/access-check/{userId}
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "canView": false,
  "reason": "blocked"
}
```

**Reasons:**
- `"blocked"` - Bạn đã chặn user này
- `"blocked_by_user"` - User này đã chặn bạn
- `"private_account"` - Account riêng tư và bạn không phải friend/follower
- `null` - Có thể xem

## Private Account API

### 1. Update Privacy Settings
```http
PUT /api/privacy/account
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "isPrivate": true
}
```

**Response:**
```json
{
  "isPrivate": true,
  "updatedAt": "2025-11-04T10:30:00"
}
```

### 2. Get Privacy Settings
```http
GET /api/privacy/account
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "isPrivate": true,
  "updatedAt": "2025-11-04T10:30:00"
}
```

### 3. Check If Account Is Private
```http
GET /api/privacy/account/{userId}
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "isPrivate": true
}
```

## Private Account Behavior

### Khi Account = Private:

#### 1. **Profile View:**
- ✅ Public users: Xem được basic info (username, avatar, bio)
- ❌ Public users: Không xem được posts, followers, following count
- ✅ Friends/Followers: Xem được tất cả

#### 2. **Posts:**
- ❌ Không hiển thị trong Explore
- ❌ Không hiển thị trong Search results
- ✅ Chỉ friends/followers xem được

#### 3. **Follow Request:**
- Khi follow private account → tạo **pending follow request**
- User cần chấp nhận mới follow được
- (Tính năng này cần implement thêm FollowRequest model)

#### 4. **Stories:** (Khi implement)
- Chỉ followers xem được

## Block Behavior

### Khi Block User:

#### 1. **Immediate Actions:**
- ❌ Xóa friendship (cả 2 chiều)
- ❌ Xóa follow (cả 2 chiều)
- ❌ Xóa pending friend requests
- ✅ Messages cũ vẫn còn nhưng không gửi được mới

#### 2. **Restrictions:**
- ❌ Không xem profile của nhau
- ❌ Không xem posts của nhau
- ❌ Không thể follow lại
- ❌ Không thể gửi friend request
- ❌ Không thể gửi messages
- ❌ Không thể comment/like posts
- ❌ Không hiển thị trong search results

#### 3. **Feed & Notifications:**
- ❌ Posts không hiển thị trong feed
- ❌ Không nhận notifications từ nhau

## Integration vào Existing Features

### PostService
```kotlin
// Feed: Loại trừ blocked users
fun getFeedPosts(userId: Long, pageable: Pageable): Page<Post> {
    val blockedIds = blockRepository.findBlockedUserIds(userId)
    val blockerIds = blockRepository.findBlockerIds(userId)
    // Filter out blocked users...
}

// User posts: Check block before returning
fun getUserPosts(userId: Long, currentUserId: Long?, pageable: Pageable): Page<Post> {
    if (blockRepository.hasBlockBetween(currentUserId, userId)) {
        return Page.empty()
    }
    // ...
}
```

### FollowService
```kotlin
fun followUser(followerId: Long, followedId: Long): Boolean {
    // Check block before allowing follow
    if (blockRepository.hasBlockBetween(followerId, followedId)) {
        throw RuntimeException("Cannot follow due to block relationship")
    }
    // ...
}
```

### FriendService
```kotlin
fun sendFriendRequest(senderId: Long, receiverId: Long): FriendRequestResponse {
    // Check block before sending request
    if (blockRepository.hasBlockBetween(senderId, receiverId)) {
        throw IllegalArgumentException("Cannot send friend request due to block")
    }
    // ...
}
```

## Flutter Integration

### 1. Models
```dart
class BlockedUser {
  final int blockId;
  final int userId;
  final String username;
  final String? fullName;
  final String? avatarUrl;
  final DateTime blockedAt;

  BlockedUser({
    required this.blockId,
    required this.userId,
    required this.username,
    this.fullName,
    this.avatarUrl,
    required this.blockedAt,
  });

  factory BlockedUser.fromJson(Map<String, dynamic> json) {
    return BlockedUser(
      blockId: json['blockId'],
      userId: json['userId'],
      username: json['username'],
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
      blockedAt: DateTime.parse(json['blockedAt']),
    );
  }
}

class AccountAccess {
  final bool canView;
  final String? reason;

  AccountAccess({
    required this.canView,
    this.reason,
  });

  factory AccountAccess.fromJson(Map<String, dynamic> json) {
    return AccountAccess(
      canView: json['canView'],
      reason: json['reason'],
    );
  }
}

class PrivacySettings {
  final bool isPrivate;
  final DateTime updatedAt;

  PrivacySettings({
    required this.isPrivate,
    required this.updatedAt,
  });

  factory PrivacySettings.fromJson(Map<String, dynamic> json) {
    return PrivacySettings(
      isPrivate: json['isPrivate'],
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }
}
```

### 2. Block Service
```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

class BlockService {
  final String baseUrl = 'http://10.0.2.2:8081/api';

  // Block user
  Future<BlockedUser> blockUser(
    String accessToken,
    int blockedUserId,
  ) async {
    final response = await http.post(
      Uri.parse('$baseUrl/blocks'),
      headers: {
        'Authorization': 'Bearer $accessToken',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'blockedUserId': blockedUserId}),
    );

    if (response.statusCode == 201) {
      return BlockedUser.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to block user');
  }

  // Unblock user
  Future<void> unblockUser(
    String accessToken,
    int blockedUserId,
  ) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/blocks/$blockedUserId'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to unblock user');
    }
  }

  // Get blocked users
  Future<List<BlockedUser>> getBlockedUsers(
    String accessToken, {
    int page = 0,
    int size = 20,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/blocks?page=$page&size=$size'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return (data['content'] as List)
          .map((json) => BlockedUser.fromJson(json))
          .toList();
    }
    throw Exception('Failed to load blocked users');
  }

  // Check if user is blocked
  Future<bool> isUserBlocked(
    String accessToken,
    int userId,
  ) async {
    final response = await http.get(
      Uri.parse('$baseUrl/blocks/check/$userId'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['isBlocked'];
    }
    throw Exception('Failed to check block status');
  }

  // Check account access
  Future<AccountAccess> checkAccountAccess(
    String accessToken,
    int userId,
  ) async {
    final response = await http.get(
      Uri.parse('$baseUrl/blocks/access-check/$userId'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      return AccountAccess.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to check account access');
  }
}
```

### 3. Privacy Service
```dart
class PrivacyService {
  final String baseUrl = 'http://10.0.2.2:8081/api/privacy';

  // Update privacy settings
  Future<PrivacySettings> updatePrivacySetting(
    String accessToken,
    bool isPrivate,
  ) async {
    final response = await http.put(
      Uri.parse('$baseUrl/account'),
      headers: {
        'Authorization': 'Bearer $accessToken',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'isPrivate': isPrivate}),
    );

    if (response.statusCode == 200) {
      return PrivacySettings.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to update privacy settings');
  }

  // Get privacy settings
  Future<PrivacySettings> getPrivacySetting(String accessToken) async {
    final response = await http.get(
      Uri.parse('$baseUrl/account'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      return PrivacySettings.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to get privacy settings');
  }

  // Check if account is private
  Future<bool> isAccountPrivate(
    String accessToken,
    int userId,
  ) async {
    final response = await http.get(
      Uri.parse('$baseUrl/account/$userId'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['isPrivate'];
    }
    throw Exception('Failed to check account privacy');
  }
}
```

### 4. Profile Screen với Block Button
```dart
class UserProfileScreen extends StatefulWidget {
  final int userId;

  UserProfileScreen({required this.userId});

  @override
  _UserProfileScreenState createState() => _UserProfileScreenState();
}

class _UserProfileScreenState extends State<UserProfileScreen> {
  final BlockService _blockService = BlockService();
  bool _isBlocked = false;
  bool _canViewProfile = true;
  String? _blockReason;

  @override
  void initState() {
    super.initState();
    _checkAccess();
  }

  Future<void> _checkAccess() async {
    try {
      final token = await _getAccessToken();
      
      // Check if blocked
      final isBlocked = await _blockService.isUserBlocked(token, widget.userId);
      
      // Check account access
      final access = await _blockService.checkAccountAccess(token, widget.userId);
      
      setState(() {
        _isBlocked = isBlocked;
        _canViewProfile = access.canView;
        _blockReason = access.reason;
      });
    } catch (e) {
      print('Error checking access: $e');
    }
  }

  Future<void> _blockUser() async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Block User'),
        content: Text('Are you sure you want to block this user?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: Text('Block', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );

    if (confirm == true) {
      try {
        final token = await _getAccessToken();
        await _blockService.blockUser(token, widget.userId);
        setState(() => _isBlocked = true);
        _showSuccess('User blocked successfully');
        Navigator.pop(context); // Go back
      } catch (e) {
        _showError('Failed to block user');
      }
    }
  }

  Future<void> _unblockUser() async {
    try {
      final token = await _getAccessToken();
      await _blockService.unblockUser(token, widget.userId);
      setState(() => _isBlocked = false);
      _checkAccess(); // Recheck access
      _showSuccess('User unblocked successfully');
    } catch (e) {
      _showError('Failed to unblock user');
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!_canViewProfile) {
      return Scaffold(
        appBar: AppBar(title: Text('Profile')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.lock, size: 64, color: Colors.grey),
              SizedBox(height: 16),
              Text(
                _getBlockMessage(),
                style: TextStyle(fontSize: 16, color: Colors.grey),
                textAlign: TextAlign.center,
              ),
              if (_isBlocked) ...[
                SizedBox(height: 24),
                ElevatedButton(
                  onPressed: _unblockUser,
                  child: Text('Unblock'),
                ),
              ],
            ],
          ),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text('Profile'),
        actions: [
          PopupMenuButton<String>(
            onSelected: (value) {
              if (value == 'block') {
                _blockUser();
              } else if (value == 'unblock') {
                _unblockUser();
              }
            },
            itemBuilder: (context) => [
              if (!_isBlocked)
                PopupMenuItem(
                  value: 'block',
                  child: Row(
                    children: [
                      Icon(Icons.block, color: Colors.red),
                      SizedBox(width: 8),
                      Text('Block User'),
                    ],
                  ),
                ),
              if (_isBlocked)
                PopupMenuItem(
                  value: 'unblock',
                  child: Row(
                    children: [
                      Icon(Icons.check_circle),
                      SizedBox(width: 8),
                      Text('Unblock User'),
                    ],
                  ),
                ),
            ],
          ),
        ],
      ),
      body: Column(
        children: [
          // Profile content...
        ],
      ),
    );
  }

  String _getBlockMessage() {
    switch (_blockReason) {
      case 'blocked':
        return 'You have blocked this user.\nUnblock to view their profile.';
      case 'blocked_by_user':
        return 'This user has blocked you.\nYou cannot view their profile.';
      case 'private_account':
        return 'This account is private.\nFollow to see their posts.';
      default:
        return 'Cannot view this profile.';
    }
  }
}
```

### 5. Settings Screen - Privacy Toggle
```dart
class SettingsScreen extends StatefulWidget {
  @override
  _SettingsScreenState createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final PrivacyService _privacyService = PrivacyService();
  bool _isPrivate = false;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadPrivacySettings();
  }

  Future<void> _loadPrivacySettings() async {
    setState(() => _isLoading = true);
    try {
      final token = await _getAccessToken();
      final settings = await _privacyService.getPrivacySetting(token);
      setState(() {
        _isPrivate = settings.isPrivate;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _showError('Failed to load privacy settings');
    }
  }

  Future<void> _togglePrivacy(bool value) async {
    try {
      final token = await _getAccessToken();
      await _privacyService.updatePrivacySetting(token, value);
      setState(() => _isPrivate = value);
      _showSuccess(value ? 'Account is now private' : 'Account is now public');
    } catch (e) {
      _showError('Failed to update privacy settings');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Privacy Settings')),
      body: ListView(
        children: [
          SwitchListTile(
            title: Text('Private Account'),
            subtitle: Text(
              _isPrivate
                  ? 'Only followers can see your posts'
                  : 'Anyone can see your posts',
            ),
            value: _isPrivate,
            onChanged: _isLoading ? null : _togglePrivacy,
            secondary: Icon(Icons.lock),
          ),
          Divider(),
          ListTile(
            leading: Icon(Icons.block),
            title: Text('Blocked Users'),
            trailing: Icon(Icons.chevron_right),
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => BlockedUsersScreen()),
              );
            },
          ),
        ],
      ),
    );
  }
}
```

## Testing

### Test block user
```bash
curl -X POST http://localhost:8081/api/blocks \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"blockedUserId": 2}'
```

### Test unblock user
```bash
curl -X DELETE http://localhost:8081/api/blocks/2 \
  -H "Authorization: Bearer {token}"
```

### Test update privacy
```bash
curl -X PUT http://localhost:8081/api/privacy/account \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"isPrivate": true}'
```

### Test access check
```bash
curl -X GET http://localhost:8081/api/blocks/access-check/2 \
  -H "Authorization: Bearer {token}"
```

## Database Migration

```sql
-- Add isPrivate column to users table
ALTER TABLE users ADD COLUMN is_private BOOLEAN DEFAULT FALSE;

-- Create blocks table
CREATE TABLE blocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_block (blocker_id, blocked_id),
    INDEX idx_blocker (blocker_id),
    INDEX idx_blocked (blocked_id)
);
```

## Key Features

✅ **Block Users**
- Block/Unblock users
- Auto remove friendships and follows when blocking
- Prevent all interactions between blocked users

✅ **Private Account**
- Toggle account privacy
- Only friends/followers can see private posts
- Protected profile view

✅ **Access Control**
- Smart access checking in PostService
- Block checks in FollowService and FriendService
- Feed filtering to exclude blocked users

✅ **Integration**
- Seamlessly integrated into existing features
- No breaking changes to existing APIs
- Backward compatible

## Best Practices

1. **Always Check Block:** Kiểm tra block relationship trước mọi action
2. **Cascade Delete:** Xóa relationships khi block
3. **Privacy First:** Respect private account settings
4. **Clear Messages:** Hiển thị lý do rõ ràng khi không thể access
5. **Performance:** Index trên blocker_id và blocked_id
