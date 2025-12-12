class FeedResponse {
  final List<PostDto> posts;
  final int currentPage;
  final int totalPages;
  final int totalItems;

  FeedResponse({
    required this.posts,
    required this.currentPage,
    required this.totalPages,
    required this.totalItems,
  });

  factory FeedResponse.fromJson(Map<String, dynamic> json) {
    return FeedResponse(
      posts: (json['posts'] as List? ?? [])
          .map((p) => PostDto.fromJson(p))
          .toList(),
      currentPage: json['currentPage'] ?? 0,
      totalPages: json['totalPages'] ?? 0,
      totalItems: json['totalItems'] ?? 0,
    );
  }
}

class PostDto {
  final int id;
  final PostUser user;
  final String? caption;
  final String visibility;
  final List<MediaFile> mediaFiles;
  final int likesCount;
  final int commentsCount;
  final bool isLiked;
  final String createdAt;
  final String? updatedAt;

  PostDto({
    required this.id,
    required this.user,
    this.caption,
    required this.visibility,
    required this.mediaFiles,
    required this.likesCount,
    required this.commentsCount,
    required this.isLiked,
    required this.createdAt,
    this.updatedAt,
  });

  // Convenience getters for UI compatibility
  bool get likedByCurrentUser => isLiked;
  int get likeCount => likesCount;
  bool get isOwner => false; // TODO: implement proper ownership check
  String? get userAvatar => user.avatarUrl;
  String get username => user.username;

  factory PostDto.fromJson(Map<String, dynamic> json) {
    // Backend có thể trả về 2 format: user object hoặc flat fields
    PostUser user;
    if (json.containsKey('user') && json['user'] is Map) {
      user = PostUser.fromJson(json['user']);
    } else {
      // Flat format từ backend (userId, username, userAvatarUrl)
      user = PostUser(
        id: json['userId'] ?? 0,
        username: json['username'] ?? '',
        fullName: json['fullName'],
        avatarUrl: json['userAvatarUrl'],
      );
    }

    return PostDto(
      id: json['id'] ?? 0,
      user: user,
      caption: json['caption'],
      visibility: json['visibility'] ?? 'PUBLIC',
      mediaFiles: (json['mediaFiles'] as List? ?? [])
          .map((m) => MediaFile.fromJson(m))
          .toList(),
      likesCount: json['likeCount'] ?? json['likesCount'] ?? 0,
      commentsCount: json['commentCount'] ?? json['commentsCount'] ?? 0,
      isLiked: json['isLiked'] ?? false,
      createdAt: json['createdAt'] ?? '',
      updatedAt: json['updatedAt'],
    );
  }
}

class PostUser {
  final int id;
  final String username;
  final String? fullName;
  final String? avatarUrl;

  PostUser({
    required this.id,
    required this.username,
    this.fullName,
    this.avatarUrl,
  });

  factory PostUser.fromJson(Map<String, dynamic> json) {
    return PostUser(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
    );
  }
}

class MediaFile {
  final String fileUrl;
  final String fileType;
  final int orderIndex;

  MediaFile({
    required this.fileUrl,
    required this.fileType,
    this.orderIndex = 0,
  });

  factory MediaFile.fromJson(Map<String, dynamic> json) {
    return MediaFile(
      fileUrl: json['fileUrl'] ?? '',
      fileType: json['fileType'] ?? 'IMAGE',
      orderIndex: json['orderIndex'] ?? 0,
    );
  }
}

class Comment {
  final int id;
  final String content;
  final PostUser user;
  final String createdAt;

  String? get userAvatar => user.avatarUrl;
  String get username => user.username;

  Comment({
    required this.id,
    required this.content,
    required this.user,
    required this.createdAt,
  });

  factory Comment.fromJson(Map<String, dynamic> json) {
    return Comment(
      id: json['id'] ?? 0,
      content: json['content'] ?? '',
      // Backend trả về flat fields (userId, username, userAvatarUrl)
      // hoặc nested object (user: {...})
      user: json['user'] != null 
          ? PostUser.fromJson(json['user'])
          : PostUser(
              id: json['userId'] ?? 0,
              username: json['username'] ?? '',
              avatarUrl: json['userAvatarUrl'], // Backend dùng 'userAvatarUrl' không phải 'avatarUrl'
            ),
      createdAt: json['createdAt'] ?? '',
    );
  }
}
