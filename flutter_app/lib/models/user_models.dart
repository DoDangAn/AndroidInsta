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

  // Added null check for JSON
  factory UserProfile.fromJson(Map<String, dynamic>? json) {
    if (json == null) {
      throw ArgumentError('JSON cannot be null');
    }
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

  static UserProfile empty() {
    return UserProfile(
      id: 0,
      username: '',
      email: '',
      fullName: null,
      bio: null,
      avatarUrl: null,
      isVerified: false,
      isActive: true,
      createdAt: '',
      updatedAt: null,
    );
  }
}

class UserStats {
  final int followersCount;
  final int followingCount;
  final int postsCount;

  UserStats({
    required this.followersCount,
    required this.followingCount,
    this.postsCount = 0,
  });

  factory UserStats.fromJson(Map<String, dynamic> json) {
    // Xử lý các trường hợp: int, Long, List<dynamic> từ Redis type metadata
    int parseCount(dynamic value) {
      if (value == null) return 0;
      if (value is int) return value;
      if (value is double) return value.toInt();
      if (value is String) return int.tryParse(value) ?? 0;
      // Trường hợp Redis trả về ["java.lang.Long", 1]
      if (value is List && value.length == 2) {
        final actualValue = value[1];
        if (actualValue is int) return actualValue;
        if (actualValue is double) return actualValue.toInt();
      }
      return 0;
    }

    return UserStats(
      followersCount: parseCount(json['followersCount']),
      followingCount: parseCount(json['followingCount']),
      postsCount: parseCount(json['postsCount']),
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
