# Comment & Reply API - AndroidInsta

## Tổng quan
API đầy đủ cho comments và replies (phản hồi comment) trong bài viết.

## Cấu trúc Comment
- **Comment gốc:** Comment trực tiếp vào post (`parentCommentId = null`)
- **Reply:** Phản hồi một comment (`parentCommentId != null`)
- **Nested:** Hỗ trợ 2 cấp (comment → reply)

## API Endpoints

### 1. Thêm Comment hoặc Reply
```http
POST /api/posts/{postId}/comments
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "content": "Great post!",
  "parentCommentId": null
}
```

**Để reply comment:**
```json
{
  "content": "I agree with you!",
  "parentCommentId": 123
}
```

**Response:**
```json
{
  "id": 456,
  "postId": 789,
  "userId": 100,
  "username": "john_doe",
  "userAvatarUrl": "https://...",
  "content": "Great post!",
  "parentCommentId": null,
  "repliesCount": 0,
  "createdAt": "2025-11-04T10:30:00"
}
```

### 2. Lấy Comments của Post
```http
GET /api/posts/{postId}/comments
Authorization: Bearer {access_token}
```

Chỉ trả về **comments gốc** (không bao gồm replies)

**Response:**
```json
[
  {
    "id": 456,
    "postId": 789,
    "userId": 100,
    "username": "john_doe",
    "userAvatarUrl": "https://...",
    "content": "Great post!",
    "parentCommentId": null,
    "repliesCount": 5,
    "createdAt": "2025-11-04T10:30:00"
  }
]
```

### 3. Lấy Replies của Comment
```http
GET /api/posts/{postId}/comments/{commentId}/replies
Authorization: Bearer {access_token}
```

**Response:**
```json
[
  {
    "id": 457,
    "postId": 789,
    "userId": 101,
    "username": "jane_smith",
    "userAvatarUrl": "https://...",
    "content": "I agree!",
    "parentCommentId": 456,
    "repliesCount": 0,
    "createdAt": "2025-11-04T10:35:00"
  }
]
```

### 4. Xóa Comment
```http
DELETE /api/posts/{postId}/comments/{commentId}
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "message": "Comment deleted successfully"
}
```

**Note:** Xóa comment gốc sẽ xóa tất cả replies của nó (CASCADE)

### 5. Đếm số Comments
```http
GET /api/posts/{postId}/comments/count
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "count": 42
}
```

## Notifications

### Comment gốc
Khi user A comment vào post của user B:
- User B nhận notification: `"A đã bình luận về bài viết của bạn: 'content'"`
- Type: `COMMENT`

### Reply comment
Khi user A reply comment của user B:
- User B nhận notification: `"A đã trả lời bình luận của bạn: 'content'"`
- Type: `REPLY`

## Sử dụng từ Flutter

### 1. Model Classes
```dart
class CommentResponse {
  final int id;
  final int postId;
  final int userId;
  final String username;
  final String? userAvatarUrl;
  final String content;
  final int? parentCommentId;
  final int repliesCount;
  final DateTime createdAt;

  CommentResponse({
    required this.id,
    required this.postId,
    required this.userId,
    required this.username,
    this.userAvatarUrl,
    required this.content,
    this.parentCommentId,
    required this.repliesCount,
    required this.createdAt,
  });

  factory CommentResponse.fromJson(Map<String, dynamic> json) {
    return CommentResponse(
      id: json['id'],
      postId: json['postId'],
      userId: json['userId'],
      username: json['username'],
      userAvatarUrl: json['userAvatarUrl'],
      content: json['content'],
      parentCommentId: json['parentCommentId'],
      repliesCount: json['repliesCount'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}
```

### 2. Comment Service
```dart
class CommentService {
  final String baseUrl = ApiConfig.baseUrl;

  // Thêm comment
  Future<CommentResponse> addComment(
    String accessToken,
    int postId,
    String content, {
    int? parentCommentId,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/posts/$postId/comments'),
      headers: {
        'Authorization': 'Bearer $accessToken',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'content': content,
        'parentCommentId': parentCommentId,
      }),
    );

    if (response.statusCode == 201) {
      return CommentResponse.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to add comment');
  }

  // Lấy comments của post
  Future<List<CommentResponse>> getPostComments(
    String accessToken,
    int postId,
  ) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/posts/$postId/comments'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => CommentResponse.fromJson(json)).toList();
    }
    throw Exception('Failed to load comments');
  }

  // Lấy replies của comment
  Future<List<CommentResponse>> getCommentReplies(
    String accessToken,
    int postId,
    int commentId,
  ) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/posts/$postId/comments/$commentId/replies'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => CommentResponse.fromJson(json)).toList();
    }
    throw Exception('Failed to load replies');
  }

  // Xóa comment
  Future<void> deleteComment(
    String accessToken,
    int postId,
    int commentId,
  ) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/api/posts/$postId/comments/$commentId'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to delete comment');
    }
  }
}
```

### 3. Comment UI Screen
```dart
class CommentsScreen extends StatefulWidget {
  final int postId;

  CommentsScreen({required this.postId});

  @override
  _CommentsScreenState createState() => _CommentsScreenState();
}

class _CommentsScreenState extends State<CommentsScreen> {
  final CommentService _commentService = CommentService();
  final TextEditingController _controller = TextEditingController();
  List<CommentResponse> _comments = [];
  bool _isLoading = true;
  int? _replyingToCommentId;
  String? _replyingToUsername;

  @override
  void initState() {
    super.initState();
    _loadComments();
  }

  Future<void> _loadComments() async {
    setState(() => _isLoading = true);
    try {
      final token = await _getAccessToken();
      final comments = await _commentService.getPostComments(token, widget.postId);
      setState(() {
        _comments = comments;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _showError('Failed to load comments');
    }
  }

  Future<void> _addComment() async {
    if (_controller.text.trim().isEmpty) return;

    try {
      final token = await _getAccessToken();
      final newComment = await _commentService.addComment(
        token,
        widget.postId,
        _controller.text.trim(),
        parentCommentId: _replyingToCommentId,
      );

      _controller.clear();
      setState(() {
        _replyingToCommentId = null;
        _replyingToUsername = null;
      });
      
      _loadComments(); // Reload to show new comment
    } catch (e) {
      _showError('Failed to add comment');
    }
  }

  void _startReply(CommentResponse comment) {
    setState(() {
      _replyingToCommentId = comment.id;
      _replyingToUsername = comment.username;
    });
    FocusScope.of(context).requestFocus(_focusNode);
  }

  void _cancelReply() {
    setState(() {
      _replyingToCommentId = null;
      _replyingToUsername = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Comments')),
      body: Column(
        children: [
          // Comments list
          Expanded(
            child: _isLoading
                ? Center(child: CircularProgressIndicator())
                : ListView.builder(
                    itemCount: _comments.length,
                    itemBuilder: (context, index) {
                      return CommentItem(
                        comment: _comments[index],
                        onReply: _startReply,
                        onDelete: _deleteComment,
                      );
                    },
                  ),
          ),
          
          // Reply indicator
          if (_replyingToUsername != null)
            Container(
              padding: EdgeInsets.all(8),
              color: Colors.grey[200],
              child: Row(
                children: [
                  Text('Replying to @$_replyingToUsername'),
                  Spacer(),
                  IconButton(
                    icon: Icon(Icons.close),
                    onPressed: _cancelReply,
                  ),
                ],
              ),
            ),
          
          // Input field
          Container(
            padding: EdgeInsets.all(8),
            decoration: BoxDecoration(
              border: Border(top: BorderSide(color: Colors.grey[300]!)),
            ),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _controller,
                    decoration: InputDecoration(
                      hintText: _replyingToUsername != null
                          ? 'Reply to @$_replyingToUsername...'
                          : 'Add a comment...',
                      border: OutlineInputBorder(),
                    ),
                  ),
                ),
                SizedBox(width: 8),
                IconButton(
                  icon: Icon(Icons.send),
                  onPressed: _addComment,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class CommentItem extends StatefulWidget {
  final CommentResponse comment;
  final Function(CommentResponse) onReply;
  final Function(int) onDelete;

  CommentItem({
    required this.comment,
    required this.onReply,
    required this.onDelete,
  });

  @override
  _CommentItemState createState() => _CommentItemState();
}

class _CommentItemState extends State<CommentItem> {
  List<CommentResponse> _replies = [];
  bool _showReplies = false;
  bool _loadingReplies = false;

  Future<void> _loadReplies() async {
    if (_replies.isNotEmpty) {
      setState(() => _showReplies = !_showReplies);
      return;
    }

    setState(() => _loadingReplies = true);
    try {
      final token = await _getAccessToken();
      final replies = await CommentService().getCommentReplies(
        token,
        widget.comment.postId,
        widget.comment.id,
      );
      setState(() {
        _replies = replies;
        _showReplies = true;
        _loadingReplies = false;
      });
    } catch (e) {
      setState(() => _loadingReplies = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        ListTile(
          leading: CircleAvatar(
            backgroundImage: widget.comment.userAvatarUrl != null
                ? NetworkImage(widget.comment.userAvatarUrl!)
                : null,
            child: widget.comment.userAvatarUrl == null
                ? Icon(Icons.person)
                : null,
          ),
          title: Text(widget.comment.username),
          subtitle: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(widget.comment.content),
              SizedBox(height: 4),
              Row(
                children: [
                  TextButton(
                    onPressed: () => widget.onReply(widget.comment),
                    child: Text('Reply'),
                  ),
                  if (widget.comment.repliesCount > 0)
                    TextButton(
                      onPressed: _loadReplies,
                      child: Text(
                        _showReplies
                            ? 'Hide replies'
                            : 'View ${widget.comment.repliesCount} replies',
                      ),
                    ),
                ],
              ),
            ],
          ),
          trailing: IconButton(
            icon: Icon(Icons.more_vert),
            onPressed: () => _showOptions(context),
          ),
        ),
        
        // Replies
        if (_showReplies && _replies.isNotEmpty)
          Padding(
            padding: EdgeInsets.only(left: 48),
            child: Column(
              children: _replies.map((reply) {
                return ListTile(
                  leading: CircleAvatar(
                    backgroundImage: reply.userAvatarUrl != null
                        ? NetworkImage(reply.userAvatarUrl!)
                        : null,
                    child: reply.userAvatarUrl == null
                        ? Icon(Icons.person, size: 16)
                        : null,
                    radius: 16,
                  ),
                  title: Text(reply.username),
                  subtitle: Text(reply.content),
                );
              }).toList(),
            ),
          ),
        
        if (_loadingReplies)
          Padding(
            padding: EdgeInsets.only(left: 48),
            child: CircularProgressIndicator(),
          ),
        
        Divider(),
      ],
    );
  }

  void _showOptions(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (context) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            leading: Icon(Icons.delete),
            title: Text('Delete'),
            onTap: () {
              Navigator.pop(context);
              widget.onDelete(widget.comment.id);
            },
          ),
        ],
      ),
    );
  }
}
```

## Testing

### Test thêm comment
```bash
curl -X POST http://localhost:8081/api/posts/1/comments \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"content": "Great post!"}'
```

### Test reply comment
```bash
curl -X POST http://localhost:8081/api/posts/1/comments \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"content": "I agree!", "parentCommentId": 123}'
```

### Test lấy comments
```bash
curl -X GET http://localhost:8081/api/posts/1/comments \
  -H "Authorization: Bearer {token}"
```

### Test lấy replies
```bash
curl -X GET http://localhost:8081/api/posts/1/comments/123/replies \
  -H "Authorization: Bearer {token}"
```

## Database Schema

```sql
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id BIGINT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_parent_comment_id (parent_comment_id)
);
```

## Best Practices

1. **Limit Nesting:** Chỉ hỗ trợ 2 cấp (comment → reply)
2. **Pagination:** Nếu có nhiều comments, thêm pagination
3. **Real-time:** Có thể dùng WebSocket để update comments real-time
4. **Moderation:** Admin có thể xóa bất kỳ comment nào
5. **Rate Limiting:** Giới hạn số comments per user per minute
