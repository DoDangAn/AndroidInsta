# Chức năng Tìm kiếm (Search)

## Tổng quan
Chức năng tìm kiếm cho phép người dùng tìm kiếm:
- **Người dùng** (Users): Tìm theo username, email, hoặc tên đầy đủ
- **Bài viết** (Posts): Tìm theo caption hoặc username của người đăng
- **Reels**: Tìm video posts
- **Tags**: Tìm hashtags

## Backend API

### Endpoints

#### 1. Tìm kiếm người dùng
```
GET /api/search/users?keyword={keyword}&page={page}&size={size}
```
- **keyword**: Từ khóa tìm kiếm (bắt buộc)
- **page**: Số trang (mặc định: 0)
- **size**: Số kết quả mỗi trang (mặc định: 20)

#### 2. Tìm kiếm bài viết
```
GET /api/search/posts?keyword={keyword}&page={page}&size={size}
```

#### 3. Tìm kiếm reels
```
GET /api/search/reels?keyword={keyword}&page={page}&size={size}
```

#### 4. Tìm kiếm tags
```
GET /api/search/tags?keyword={keyword}&page={page}&size={size}
```

#### 5. Tìm kiếm tổng hợp
```
GET /api/search/all?keyword={keyword}
```
Trả về top 10 kết quả của mỗi loại (users, posts, tags)

#### 6. Trending tags
```
GET /api/search/trending/tags?page={page}&size={size}
```
Lấy danh sách tags phổ biến nhất

#### 7. Gợi ý tìm kiếm (Autocomplete)
```
GET /api/search/suggestions?q={query}&limit={limit}
```
- **q**: Từ khóa (tối thiểu 2 ký tự)
- **limit**: Số lượng gợi ý (mặc định: 5)

## Flutter Implementation

### Models
- `UserSearchResult`: Kết quả tìm kiếm người dùng
- `PostSearchResult`: Kết quả tìm kiếm bài viết
- `TagSearchResult`: Kết quả tìm kiếm tags
- `SearchAllResult`: Kết quả tìm kiếm tổng hợp
- `SearchSuggestions`: Gợi ý tìm kiếm
- `SearchPageResponse<T>`: Response có phân trang

### Service
`SearchService` cung cấp các methods:
- `searchUsers()`: Tìm kiếm người dùng
- `searchPosts()`: Tìm kiếm bài viết
- `searchReels()`: Tìm kiếm reels
- `searchTags()`: Tìm kiếm tags
- `searchAll()`: Tìm kiếm tổng hợp
- `getTrendingTags()`: Lấy trending tags
- `getSearchSuggestions()`: Lấy gợi ý tìm kiếm

### UI Components

#### SearchScreen
Màn hình tìm kiếm chính với:
- **Search bar**: Thanh tìm kiếm với autocomplete
- **Tabs**: 4 tabs (Tất cả, Người dùng, Bài viết, Tags)
- **Trending section**: Hiển thị trending tags khi chưa tìm kiếm
- **Suggestions**: Hiển thị gợi ý khi đang gõ (≥2 ký tự)
- **Results**: Hiển thị kết quả tìm kiếm với pagination

#### Features
1. **Autocomplete**: Gợi ý tự động khi gõ từ khóa
2. **Tabs**: Chuyển đổi giữa các loại kết quả
3. **Pagination**: Tải thêm kết quả khi scroll xuống cuối
4. **Navigation**: Click vào kết quả để xem chi tiết
5. **Trending**: Hiển thị tags phổ biến

## Cách sử dụng

### 1. Từ Home Screen
- Nhấn vào icon Search ở bottom navigation bar
- Hoặc chọn tab thứ 2 (Search icon)

### 2. Tìm kiếm
- Nhập từ khóa vào search bar
- Xem gợi ý tự động (nếu có)
- Nhấn Enter hoặc chọn gợi ý để tìm kiếm
- Chuyển đổi giữa các tab để xem kết quả khác nhau

### 3. Xem kết quả
- **Users**: Click vào user để xem profile
- **Posts**: Click vào post để xem chi tiết
- **Tags**: Click vào tag để tìm kiếm bài viết với tag đó

### 4. Trending Tags
- Khi chưa tìm kiếm, màn hình hiển thị trending tags
- Click vào tag để tìm kiếm bài viết liên quan

## Database Queries

### UserRepository
```kotlin
@Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.fullName LIKE %:keyword%")
fun searchUsers(keyword: String): List<User>
```

### PostRepository
```kotlin
@Query("""
    SELECT DISTINCT p FROM Post p 
    LEFT JOIN p.user u 
    WHERE LOWER(p.caption) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY p.createdAt DESC
""")
fun searchPosts(@Param("keyword") keyword: String, pageable: Pageable): Page<Post>
```

### TagRepository
```kotlin
@Query("""
    SELECT t FROM Tag t 
    WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY t.name ASC
""")
fun searchTags(@Param("keyword") keyword: String, pageable: Pageable): Page<Tag>

@Query("""
    SELECT t FROM Tag t 
    LEFT JOIN t.posts p
    GROUP BY t.id
    ORDER BY COUNT(p.id) DESC
""")
fun findTrendingTags(pageable: Pageable): Page<Tag>
```

## Cải tiến trong tương lai
1. **Search history**: Lưu lịch sử tìm kiếm
2. **Advanced filters**: Lọc theo ngày, location, etc.
3. **Search by image**: Tìm kiếm bằng hình ảnh
4. **Voice search**: Tìm kiếm bằng giọng nói
5. **Saved searches**: Lưu các tìm kiếm thường dùng
6. **Search analytics**: Thống kê từ khóa phổ biến
