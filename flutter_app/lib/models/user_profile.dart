class UserSummary {
  final int id;
  final String username;
  final String? fullName;
  final String? avatarUrl;

  UserSummary({
    required this.id,
    required this.username,
    this.fullName,
    this.avatarUrl,
  });

  factory UserSummary.fromJson(Map<String, dynamic> json) {
    return UserSummary(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
    );
  }
}

class PostFile {
  final String fileUrl;

  PostFile({required this.fileUrl});

  factory PostFile.fromJson(Map<String, dynamic> json) {
    return PostFile(fileUrl: json['fileUrl'] ?? '');
  }
}

class Post {
  final int id;
  final List<PostFile> mediaFiles;

  Post({required this.id, required this.mediaFiles});

  factory Post.fromJson(Map<String, dynamic> json) {
    return Post(
      id: json['id'] ?? 0,
      mediaFiles: (json['mediaFiles'] as List? ?? [])
          .map((m) => PostFile.fromJson(m as Map<String, dynamic>))
          .toList(),
    );
  }
}

class UserProfile {
  final int id;
  final String username;
  final String? fullName;
  final String? bio;
  final String avatarUrl;
  final List<Post> posts;
  final List<UserSummary> followers;
  final List<UserSummary> following;

  UserProfile({
    required this.id,
    required this.username,
    this.fullName,
    this.bio,
    required this.avatarUrl,
    required this.posts,
    required this.followers,
    required this.following,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      fullName: json['fullName'],
      bio: json['bio'],
      avatarUrl: json['avatarUrl'] ?? 'https://via.placeholder.com/150',
      posts: (json['posts'] as List? ?? []).map((i) => Post.fromJson(i as Map<String,dynamic>)).toList(),
      followers: (json['followers'] as List? ?? []).map((i) => UserSummary.fromJson(i as Map<String,dynamic>)).toList(),
      following: (json['following'] as List? ?? []).map((i) => UserSummary.fromJson(i as Map<String,dynamic>)).toList(),
    );
  }
}
