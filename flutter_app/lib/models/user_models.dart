class UserProfile {
  final int id;
  final String username;
  final String email;
  final String? fullName;
  final String? bio;
  final String? avatarUrl;
  final bool isVerified;
  final bool isActive;
  final String createdAt;
  final String? updatedAt;

  UserProfile({
    required this.id,
    required this.username,
    required this.email,
    this.fullName,
    this.bio,
    this.avatarUrl,
    this.isVerified = false,
    this.isActive = true,
    required this.createdAt,
    this.updatedAt,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      email: json['email'] ?? '',
      fullName: json['fullName'],
      bio: json['bio'],
      avatarUrl: json['avatarUrl'],
      isVerified: json['isVerified'] ?? false,
      isActive: json['isActive'] ?? true,
      createdAt: json['createdAt'] ?? '',
      updatedAt: json['updatedAt'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'email': email,
      'fullName': fullName,
      'bio': bio,
      'avatarUrl': avatarUrl,
      'isVerified': isVerified,
      'isActive': isActive,
      'createdAt': createdAt,
      'updatedAt': updatedAt,
    };
  }
}

class UserStats {
  final int followersCount;
  final int followingCount;
  final bool isFollowing;

  UserStats({
    required this.followersCount,
    required this.followingCount,
    required this.isFollowing,
  });

  factory UserStats.fromJson(Map<String, dynamic> json) {
    return UserStats(
      followersCount: json['followersCount'] ?? 0,
      followingCount: json['followingCount'] ?? 0,
      isFollowing: json['isFollowing'] ?? false,
    );
  }
}

class UserSearchResult {
  final int id;
  final String username;
  final String? fullName;
  final String? avatarUrl;
  final bool isVerified;
  final bool isFollowing;

  UserSearchResult({
    required this.id,
    required this.username,
    this.fullName,
    this.avatarUrl,
    this.isVerified = false,
    this.isFollowing = false,
  });

  factory UserSearchResult.fromJson(Map<String, dynamic> json) {
    return UserSearchResult(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
      isVerified: json['isVerified'] ?? false,
      isFollowing: json['isFollowing'] ?? false,
    );
  }
}
