# Search API - AndroidInsta

## Tổng quan
API tìm kiếm đầy đủ cho users, posts, reels, và tags với pagination và filtering.

## Endpoints

### 1. Tìm kiếm Users
```http
GET /api/search/users?keyword={keyword}&page=0&size=20
```

**Query Parameters:**
- `keyword` (required): Từ khóa tìm kiếm (username, email)
- `page` (optional, default=0): Số trang
- `size` (optional, default=20): Số kết quả mỗi trang

**Response:**
```json
{
  "content": [
    {
      "id": 123,
      "username": "john_doe",
      "fullName": "John Doe",
      "avatarUrl": "https://...",
      "isVerified": true,
      "followersCount": 1500
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

### 2. Tìm kiếm Posts
```http
GET /api/search/posts?keyword={keyword}&page=0&size=20
```

**Query Parameters:**
- `keyword` (required): Từ khóa tìm kiếm (caption, username)
- `page` (optional, default=0)
- `size` (optional, default=20)

**Response:**
```json
{
  "content": [
    {
      "id": 456,
      "userId": 123,
      "username": "john_doe",
      "userAvatarUrl": "https://...",
      "caption": "Beautiful sunset at the beach",
      "mediaFiles": [
        {
          "fileUrl": "https://...",
          "fileType": "IMAGE",
          "thumbnailUrl": null
        }
      ],
      "likeCount": 250,
      "commentCount": 42,
      "createdAt": "2025-11-04T15:30:00"
    }
  ],
  "totalElements": 100
}
```

### 3. Tìm kiếm Reels (Video Posts)
```http
GET /api/search/reels?keyword={keyword}&page=0&size=20
```

Giống như search posts nhưng chỉ trả về posts có video.

**Response:** Giống như search posts

### 4. Tìm kiếm Tags
```http
GET /api/search/tags?keyword={keyword}&page=0&size=20
```

**Query Parameters:**
- `keyword` (required): Từ khóa tag (vd: travel, food)
- `page`, `size`: Phân trang

**Response:**
```json
{
  "content": [
    {
      "id": 10,
      "name": "travel",
      "postsCount": 5420
    },
    {
      "id": 15,
      "name": "traveling",
      "postsCount": 3200
    }
  ],
  "totalElements": 25
}
```

### 5. Tìm kiếm Tổng hợp (All)
```http
GET /api/search/all?keyword={keyword}
```

Trả về top 10 kết quả của mỗi loại: users, posts, tags

**Response:**
```json
{
  "users": [
    {
      "id": 123,
      "username": "john_doe",
      "fullName": "John Doe",
      "avatarUrl": "https://...",
      "isVerified": true,
      "followersCount": 1500
    }
  ],
  "posts": [
    {
      "id": 456,
      "userId": 123,
      "username": "john_doe",
      "caption": "Beautiful sunset",
      "mediaFiles": [...],
      "likeCount": 250,
      "commentCount": 42
    }
  ],
  "tags": [
    {
      "id": 10,
      "name": "travel",
      "postsCount": 5420
    }
  ]
}
```

### 6. Trending Tags
```http
GET /api/search/trending/tags?page=0&size=20
```

Lấy danh sách tags phổ biến nhất (có nhiều posts nhất)

**Response:** Giống như search tags

### 7. Search Suggestions (Auto-complete)
```http
GET /api/search/suggestions?q={query}&limit=5
```

Gợi ý tìm kiếm khi user đang gõ (tối thiểu 2 ký tự)

**Query Parameters:**
- `q` (required): Từ khóa (tối thiểu 2 ký tự)
- `limit` (optional, default=5): Số gợi ý mỗi loại

**Response:**
```json
{
  "users": [
    {
      "id": 123,
      "username": "john_doe",
      "fullName": "John Doe",
      "avatarUrl": "https://...",
      "isVerified": true,
      "followersCount": 1500
    }
  ],
  "tags": [
    {
      "id": 10,
      "name": "travel",
      "postsCount": 5420
    }
  ]
}
```

## Sử dụng từ Flutter

### 1. Search Users
```dart
Future<List<UserSearchResult>> searchUsers(String keyword, int page) async {
  final response = await http.get(
    Uri.parse('${ApiConfig.baseUrl}/api/search/users?keyword=$keyword&page=$page&size=20'),
    headers: {'Authorization': 'Bearer $accessToken'},
  );
  
  if (response.statusCode == 200) {
    final data = jsonDecode(response.body);
    return (data['content'] as List)
        .map((json) => UserSearchResult.fromJson(json))
        .toList();
  }
  throw Exception('Failed to search users');
}
```

### 2. Search All (Tab Search UI)
```dart
Future<SearchAllResult> searchAll(String keyword) async {
  final response = await http.get(
    Uri.parse('${ApiConfig.baseUrl}/api/search/all?keyword=$keyword'),
    headers: {'Authorization': 'Bearer $accessToken'},
  );
  
  if (response.statusCode == 200) {
    return SearchAllResult.fromJson(jsonDecode(response.body));
  }
  throw Exception('Failed to search');
}

// Hiển thị trong UI với tabs
Widget buildSearchResults(SearchAllResult result) {
  return DefaultTabController(
    length: 3,
    child: Column(
      children: [
        TabBar(tabs: [
          Tab(text: 'Users (${result.users.length})'),
          Tab(text: 'Posts (${result.posts.length})'),
          Tab(text: 'Tags (${result.tags.length})'),
        ]),
        Expanded(
          child: TabBarView(children: [
            UserListView(users: result.users),
            PostGridView(posts: result.posts),
            TagListView(tags: result.tags),
          ]),
        ),
      ],
    ),
  );
}
```

### 3. Search Suggestions (Auto-complete)
```dart
class SearchScreen extends StatefulWidget {
  @override
  _SearchScreenState createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  Timer? _debounce;
  List<dynamic> suggestions = [];
  
  @override
  void initState() {
    super.initState();
    _searchController.addListener(_onSearchChanged);
  }
  
  void _onSearchChanged() {
    if (_debounce?.isActive ?? false) _debounce!.cancel();
    _debounce = Timer(Duration(milliseconds: 500), () {
      if (_searchController.text.length >= 2) {
        _getSuggestions(_searchController.text);
      }
    });
  }
  
  Future<void> _getSuggestions(String query) async {
    final response = await http.get(
      Uri.parse('${ApiConfig.baseUrl}/api/search/suggestions?q=$query&limit=5'),
      headers: {'Authorization': 'Bearer $accessToken'},
    );
    
    if (response.statusCode == 200) {
      setState(() {
        suggestions = jsonDecode(response.body);
      });
    }
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: TextField(
          controller: _searchController,
          decoration: InputDecoration(
            hintText: 'Search...',
            border: InputBorder.none,
          ),
        ),
      ),
      body: suggestions.isEmpty
          ? _buildTrendingTags()
          : _buildSuggestions(),
    );
  }
}
```

### 4. Trending Tags
```dart
Future<List<TagSearchResult>> getTrendingTags() async {
  final response = await http.get(
    Uri.parse('${ApiConfig.baseUrl}/api/search/trending/tags?page=0&size=20'),
    headers: {'Authorization': 'Bearer $accessToken'},
  );
  
  if (response.statusCode == 200) {
    final data = jsonDecode(response.body);
    return (data['content'] as List)
        .map((json) => TagSearchResult.fromJson(json))
        .toList();
  }
  throw Exception('Failed to get trending tags');
}
```

## Search Flow trong App

```
┌─────────────────────────────────────────────────────┐
│ Search Screen                                       │
│ ┌────────────────────────────────────────────────┐ │
│ │ Search Bar: [Type to search...]                │ │
│ └────────────────────────────────────────────────┘ │
│                                                     │
│ ┌─────────────────────────────────────────────────┤
│ │ User gõ < 2 ký tự:                              │
│ │   → Hiển thị Trending Tags                      │
│ └─────────────────────────────────────────────────┤
│                                                     │
│ ┌─────────────────────────────────────────────────┤
│ │ User gõ >= 2 ký tự:                             │
│ │   → Debounce 500ms                              │
│ │   → Call /api/search/suggestions                │
│ │   → Show dropdown suggestions                    │
│ └─────────────────────────────────────────────────┤
│                                                     │
│ ┌─────────────────────────────────────────────────┤
│ │ User nhấn Enter hoặc chọn suggestion:          │
│ │   → Call /api/search/all                        │
│ │   → Show tabs: Users | Posts | Tags             │
│ │   → Cho phép load more với pagination           │
│ └─────────────────────────────────────────────────┤
└─────────────────────────────────────────────────────┘
```

## Best Practices

### 1. Debounce Search Input
```dart
// Tránh gọi API liên tục khi user đang gõ
Timer? _debounce;

void _onSearchChanged() {
  if (_debounce?.isActive ?? false) _debounce!.cancel();
  _debounce = Timer(Duration(milliseconds: 500), () {
    _performSearch();
  });
}
```

### 2. Cache Search Results
```dart
// Cache kết quả để tránh gọi lại khi quay lại
Map<String, SearchAllResult> _searchCache = {};

Future<SearchAllResult> searchWithCache(String keyword) async {
  if (_searchCache.containsKey(keyword)) {
    return _searchCache[keyword]!;
  }
  
  final result = await searchAll(keyword);
  _searchCache[keyword] = result;
  return result;
}
```

### 3. Empty State
```dart
Widget _buildEmptyState() {
  return Center(
    child: Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Icon(Icons.search, size: 64, color: Colors.grey),
        SizedBox(height: 16),
        Text('No results found'),
        SizedBox(height: 8),
        Text('Try different keywords'),
      ],
    ),
  );
}
```

### 4. Loading State
```dart
bool _isLoading = false;

Widget _buildSearchResults() {
  if (_isLoading) {
    return Center(child: CircularProgressIndicator());
  }
  
  if (results.isEmpty) {
    return _buildEmptyState();
  }
  
  return _buildResultsList();
}
```

## Testing

### Test Search Users
```bash
curl -X GET "http://localhost:8081/api/search/users?keyword=john&page=0&size=20" \
  -H "Authorization: Bearer {token}"
```

### Test Search All
```bash
curl -X GET "http://localhost:8081/api/search/all?keyword=travel" \
  -H "Authorization: Bearer {token}"
```

### Test Suggestions
```bash
curl -X GET "http://localhost:8081/api/search/suggestions?q=jo&limit=5" \
  -H "Authorization: Bearer {token}"
```

### Test Trending Tags
```bash
curl -X GET "http://localhost:8081/api/search/trending/tags?page=0&size=10" \
  -H "Authorization: Bearer {token}"
```

## Performance Tips

1. **Index Database:** Thêm index cho các cột search
```sql
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_posts_caption ON posts(caption);
CREATE INDEX idx_tags_name ON tags(name);
```

2. **Limit Results:** Giới hạn số kết quả mỗi trang (10-20)

3. **Use Pagination:** Luôn dùng pagination cho search results

4. **Cache Trending Tags:** Cache trending tags vì ít thay đổi
```kotlin
@Cacheable("trending-tags")
fun getTrendingTags(pageable: Pageable): Page<TagSearchResult>
```

## Error Handling

```dart
try {
  final results = await searchAll(keyword);
  // Handle success
} on SocketException {
  // No internet connection
  showError('No internet connection');
} on HttpException {
  // Server error
  showError('Server error. Try again later');
} on FormatException {
  // Invalid response
  showError('Invalid response from server');
} catch (e) {
  // Unknown error
  showError('Something went wrong');
}
```
