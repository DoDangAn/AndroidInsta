# Admin API Endpoints

Các endpoint quản trị cho admin quản lý hệ thống AndroidInsta.

## Authentication
Tất cả các endpoint admin yêu cầu:
- Role: `ADMIN`
- Header: `Authorization: Bearer <access_token>`

---

## 👥 User Management

### 1. Get All Users (with search & pagination)
```http
GET /api/admin/users?keyword=john&page=0&size=20&sortBy=createdAt&direction=DESC
```

**Query Parameters:**
- `keyword` (optional): Tìm theo username hoặc email
- `page` (default: 0): Trang hiện tại
- `size` (default: 20): Số lượng items per page
- `sortBy` (default: createdAt): Sắp xếp theo field (createdAt, username, email)
- `direction` (default: DESC): Hướng sắp xếp (ASC, DESC)

**Response:**
```json
{
  "success": true,
  "users": [
    {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "avatarUrl": "https://...",
      "isVerified": false,
      "isActive": true,
      "roleName": "USER",
      "createdAt": "2025-11-01T10:00:00",
      "updatedAt": null
    }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalItems": 100
}
```

### 2. Get User By ID (with details)
```http
GET /api/admin/users/{userId}
```

**Response:**
```json
{
  "success": true,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "bio": "Love coding!",
    "avatarUrl": "https://...",
    "isVerified": false,
    "isActive": true,
    "roleName": "USER",
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": null,
    "postsCount": 25,
    "followersCount": 150,
    "followingCount": 80
  }
}
```

### 3. Ban User
```http
PUT /api/admin/users/{userId}/ban
```

Sets `isActive = false`. User cannot login.

**Response:**
```json
{
  "success": true,
  "message": "User banned successfully"
}
```

### 4. Unban User
```http
PUT /api/admin/users/{userId}/unban
```

Sets `isActive = true`.

### 5. Verify User
```http
PUT /api/admin/users/{userId}/verify
```

Sets `isVerified = true`. Adds verified badge.

### 6. Unverify User
```http
PUT /api/admin/users/{userId}/unverify
```

Sets `isVerified = false`.

### 7. Delete User
```http
DELETE /api/admin/users/{userId}
```

Permanently deletes user and all related data (posts, comments, likes, etc.).

**Response:**
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

### 8. Get User Statistics
```http
GET /api/admin/users/{userId}/stats
```

**Response:**
```json
{
  "success": true,
  "stats": {
    "userId": 1,
    "username": "john_doe",
    "postsCount": 25,
    "followersCount": 150,
    "followingCount": 80,
    "likesGivenCount": 320,
    "likesReceivedCount": 450,
    "commentsGivenCount": 120,
    "commentsReceivedCount": 200
  }
}
```

---

## 📝 Post Management

### 1. Get All Posts
```http
GET /api/admin/posts
```

Returns all posts in the system.

### 2. Search Posts
```http
GET /api/admin/posts/search?keyword=sunset&page=0&size=10&sortBy=createdAt&direction=DESC
```

**Query Parameters:**
- `keyword` (optional): Search in caption or username
- `page`, `size`, `sortBy`, `direction`: Same as user search

### 3. Delete Post
```http
DELETE /api/admin/posts/{postId}
```

Permanently deletes post and all related data.

### 4. Hide Post
```http
PUT /api/admin/posts/{postId}/hide
```

Sets `visibility = HIDDEN`. Post won't appear in feeds.

**Response:**
```json
{
  "success": true,
  "message": "Post hidden successfully"
}
```

### 5. Unhide Post
```http
PUT /api/admin/posts/{postId}/unhide
```

Sets `visibility = PUBLIC`.

---

## � Comment Management

### 1. Get All Comments
```http
GET /api/admin/comments?page=0&size=50
```

**Query Parameters:**
- `page` (default: 0): Trang hiện tại
- `size` (default: 50): Số lượng comments per page

**Response:**
```json
{
  "success": true,
  "comments": [
    {
      "id": 123,
      "content": "Great post!",
      "postId": 45,
      "postCaption": "Beautiful sunset",
      "userId": 10,
      "username": "john_doe",
      "userAvatarUrl": "https://...",
      "parentCommentId": null,
      "createdAt": "2025-11-14T10:00:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 10,
  "totalItems": 500
}
```

### 2. Get Comments by Post
```http
GET /api/admin/comments/post/{postId}
```

Returns all comments (including replies) for a specific post.

### 3. Get Comments by User
```http
GET /api/admin/comments/user/{userId}
```

Returns all comments made by a specific user.

### 4. Delete Comment
```http
DELETE /api/admin/comments/{commentId}
```

Admin can delete any comment without permission check.

**Response:**
```json
{
  "success": true,
  "message": "Comment deleted successfully by admin"
}
```

### 5. Bulk Delete Comments
```http
DELETE /api/admin/comments/bulk
```

**Request Body:**
```json
[123, 456, 789]
```

**Response:**
```json
{
  "success": true,
  "message": "Deleted 3 comments",
  "deletedCount": 3
}
```

---

## �📊 Statistics

### 1. Overview Statistics
```http
GET /api/admin/stats/overview
```

**Response:**
```json
{
  "success": true,
  "stats": {
    "totalUsers": 1000,
    "activeUsers": 950,
    "verifiedUsers": 50,
    "totalPosts": 5000,
    "totalLikes": 25000,
    "totalComments": 8000,
    "totalFollows": 3000,
    "newUsersToday": 15,
    "newPostsToday": 50
  }
}
```

### 2. User Statistics Over Time
```http
GET /api/admin/stats/users?period=7d
```

**Query Parameters:**
- `period` (default: 7d): Time period
  - Format: `{number}{unit}`
  - Units: `d` (days), `w` (weeks), `m` (months), `y` (years)
  - Examples: `7d`, `2w`, `1m`, `1y`

**Response:**
```json
{
  "success": true,
  "stats": {
    "period": "7d",
    "data": [
      {"date": "2025-11-07", "count": 10},
      {"date": "2025-11-08", "count": 15},
      {"date": "2025-11-09", "count": 12}
    ]
  }
}
```

### 3. Post Statistics Over Time
```http
GET /api/admin/stats/posts?period=30d
```

Same format as user stats.

### 4. Engagement Statistics
```http
GET /api/admin/stats/engagement?period=7d
```

**Response:**
```json
{
  "success": true,
  "stats": {
    "likes": {
      "period": "7d",
      "data": [...]
    },
    "comments": {
      "period": "7d",
      "data": [...]
    },
    "follows": {
      "period": "7d",
      "data": [...]
    }
  }
}
```

### 5. Top Users
```http
GET /api/admin/stats/top-users?type=followers&limit=10
```

**Query Parameters:**
- `type`: `followers`, `posts`, or `likes`
- `limit` (default: 10): Number of top users

**Response:**
```json
{
  "success": true,
  "topUsers": [
    {
      "id": 5,
      "username": "influencer123",
      "fullName": "Top Influencer",
      "avatarUrl": "https://...",
      "isVerified": true,
      "count": 5000,
      "type": "followers"
    }
  ]
}
```

### 6. Top Posts
```http
GET /api/admin/stats/top-posts?type=likes&limit=10
```

**Query Parameters:**
- `type`: `likes` or `comments`
- `limit` (default: 10): Number of top posts

**Response:**
```json
{
  "success": true,
  "topPosts": [
    {
      "id": 123,
      "caption": "Amazing sunset!",
      "username": "john_doe",
      "userId": 1,
      "likesCount": 500,
      "commentsCount": 120,
      "createdAt": "2025-11-10T15:30:00"
    }
  ]
}
```

---

## 🔐 Security Notes

1. **Role-Based Access:** All endpoints require `ADMIN` role
2. **JWT Token:** Must include valid access token in Authorization header
3. **Audit Logging:** Consider logging all admin actions for compliance
4. **Rate Limiting:** Implement rate limiting for admin endpoints

---

## 📋 Common Use Cases

### Ban Spam User
```bash
# 1. Search for user
GET /api/admin/users?keyword=spammer

# 2. Get user details
GET /api/admin/users/123

# 3. Ban user
PUT /api/admin/users/123/ban
```

### Remove Inappropriate Post
```bash
# 1. Search posts
GET /api/admin/posts/search?keyword=inappropriate

# 2. Hide post (soft delete)
PUT /api/admin/posts/456/hide

# 3. Or delete permanently
DELETE /api/admin/posts/456
```

### Moderate Comments
```bash
# 1. View all recent comments
GET /api/admin/comments?page=0&size=50

# 2. Check comments on specific post
GET /api/admin/comments/post/123

# 3. Delete inappropriate comment
DELETE /api/admin/comments/789

# 4. Bulk delete spam comments
DELETE /api/admin/comments/bulk
Body: [101, 102, 103, 104]
```

### View System Health
```bash
# Get overview
GET /api/admin/stats/overview

# Check growth trend
GET /api/admin/stats/users?period=30d
GET /api/admin/stats/posts?period=30d

# See engagement
GET /api/admin/stats/engagement?period=7d
```

---

## 🚀 Next Steps

**Recommended additional features:**
1. Report management endpoints
2. Content moderation queue
3. Bulk operations (ban multiple users)
4. Export data to CSV/Excel
5. Activity logs viewer
6. Real-time dashboard WebSocket

