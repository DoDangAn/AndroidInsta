class UserSearchResult {
  final int id;
  final String username;
  final String? fullName;
  final String? avatarUrl;
  final bool isVerified;
  final int followersCount;
  final bool isFollowing;

  UserSearchResult({
    required this.id,
    required this.username,
    this.fullName,
    this.avatarUrl,
    required this.isVerified,
    required this.followersCount,
    this.isFollowing = false,
  });

  factory UserSearchResult.fromJson(Map<String, dynamic> json) {
    return UserSearchResult(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
      isVerified: json['isVerified'] ?? false,
      followersCount: json['followersCount'] ?? 0,
      isFollowing: json['isFollowing'] ?? false,
    );
  }
}

class PostSearchResult {
  final int id;
  final int userId;
  final String username;
  final String? userAvatarUrl;
  final String? caption;
  final List<MediaFileInfo> mediaFiles;
  final int likeCount;
  final int commentCount;
  final String? createdAt;

  PostSearchResult({
    required this.id,
    required this.userId,
    required this.username,
    this.userAvatarUrl,
    this.caption,
    required this.mediaFiles,
    required this.likeCount,
    required this.commentCount,
    this.createdAt,
  });

  factory PostSearchResult.fromJson(Map<String, dynamic> json) {
    return PostSearchResult(
      id: json['id'] ?? 0,
      userId: json['userId'] ?? 0,
      username: json['username'] ?? '',
      userAvatarUrl: json['userAvatarUrl'],
      caption: json['caption'],
      mediaFiles: (json['mediaFiles'] as List? ?? [])
          .map((m) => MediaFileInfo.fromJson(m))
          .toList(),
      likeCount: json['likeCount'] ?? 0,
      commentCount: json['commentCount'] ?? 0,
      createdAt: json['createdAt'],
    );
  }
}

class MediaFileInfo {
  final String fileUrl;
  final String fileType;
  final String? thumbnailUrl;

  MediaFileInfo({
    required this.fileUrl,
    required this.fileType,
    this.thumbnailUrl,
  });

  factory MediaFileInfo.fromJson(Map<String, dynamic> json) {
    return MediaFileInfo(
      fileUrl: json['fileUrl'] ?? '',
      fileType: json['fileType'] ?? 'IMAGE',
      thumbnailUrl: json['thumbnailUrl'],
    );
  }
}

class TagSearchResult {
  final int id;
  final String name;
  final int postsCount;

  TagSearchResult({
    required this.id,
    required this.name,
    required this.postsCount,
  });

  factory TagSearchResult.fromJson(Map<String, dynamic> json) {
    return TagSearchResult(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      postsCount: json['postsCount'] ?? 0,
    );
  }
}

class SearchAllResult {
  final List<UserSearchResult> users;
  final List<PostSearchResult> posts;
  final List<TagSearchResult> tags;

  SearchAllResult({
    required this.users,
    required this.posts,
    required this.tags,
  });

  factory SearchAllResult.fromJson(Map<String, dynamic> json) {
    return SearchAllResult(
      users: (json['users'] as List? ?? [])
          .map((u) => UserSearchResult.fromJson(u))
          .toList(),
      posts: (json['posts'] as List? ?? [])
          .map((p) => PostSearchResult.fromJson(p))
          .toList(),
      tags: (json['tags'] as List? ?? [])
          .map((t) => TagSearchResult.fromJson(t))
          .toList(),
    );
  }
}

class SearchSuggestions {
  final List<UserSearchResult> users;
  final List<TagSearchResult> tags;

  SearchSuggestions({
    required this.users,
    required this.tags,
  });

  factory SearchSuggestions.fromJson(Map<String, dynamic> json) {
    return SearchSuggestions(
      users: (json['users'] as List? ?? [])
          .map((u) => UserSearchResult.fromJson(u))
          .toList(),
      tags: (json['tags'] as List? ?? [])
          .map((t) => TagSearchResult.fromJson(t))
          .toList(),
    );
  }
}

class SearchPageResponse<T> {
  final List<T> content;
  final int totalPages;
  final int totalElements;
  final int number;
  final int size;

  SearchPageResponse({
    required this.content,
    required this.totalPages,
    required this.totalElements,
    required this.number,
    required this.size,
  });

  factory SearchPageResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) {
    return SearchPageResponse(
      content: (json['content'] as List? ?? [])
          .map((item) => fromJsonT(item as Map<String, dynamic>))
          .toList(),
      totalPages: json['totalPages'] ?? 0,
      totalElements: json['totalElements'] ?? 0,
      number: json['number'] ?? 0,
      size: json['size'] ?? 0,
    );
  }
}
