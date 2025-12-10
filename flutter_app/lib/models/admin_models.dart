class AdminUserDetail {
  final int id;
  final String username;
  final String email;
  final String? fullName;
  final String? bio;
  final String? avatarUrl;
  final bool isVerified;
  final bool isActive;
  final bool isBanned;
  final String? banReason;
  final List<String> roles;
  final String createdAt;
  final String? lastLoginAt;

  AdminUserDetail({
    required this.id,
    required this.username,
    required this.email,
    this.fullName,
    this.bio,
    this.avatarUrl,
    required this.isVerified,
    required this.isActive,
    required this.isBanned,
    this.banReason,
    required this.roles,
    required this.createdAt,
    this.lastLoginAt,
  });

  factory AdminUserDetail.fromJson(Map<String, dynamic> json) {
    return AdminUserDetail(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      email: json['email'] ?? '',
      fullName: json['fullName'],
      bio: json['bio'],
      avatarUrl: json['avatarUrl'],
      isVerified: json['isVerified'] ?? false,
      isActive: json['isActive'] ?? true,
      isBanned: json['isBanned'] ?? false,
      banReason: json['banReason'],
      roles: List<String>.from(json['roles'] ?? []),
      createdAt: json['createdAt'] ?? '',
      lastLoginAt: json['lastLoginAt'],
    );
  }
}

class AdminStats {
  final int totalUsers;
  final int activeUsers;
  final int bannedUsers;
  final int verifiedUsers;
  final int totalPosts;
  final int totalComments;
  final int totalLikes;
  final int newUsersToday;
  final int newPostsToday;

  AdminStats({
    required this.totalUsers,
    required this.activeUsers,
    required this.bannedUsers,
    required this.verifiedUsers,
    required this.totalPosts,
    required this.totalComments,
    required this.totalLikes,
    required this.newUsersToday,
    required this.newPostsToday,
  });

  factory AdminStats.fromJson(Map<String, dynamic> json) {
    return AdminStats(
      totalUsers: json['totalUsers'] ?? 0,
      activeUsers: json['activeUsers'] ?? 0,
      bannedUsers: json['bannedUsers'] ?? 0,
      verifiedUsers: json['verifiedUsers'] ?? 0,
      totalPosts: json['totalPosts'] ?? 0,
      totalComments: json['totalComments'] ?? 0,
      totalLikes: json['totalLikes'] ?? 0,
      newUsersToday: json['newUsersToday'] ?? 0,
      newPostsToday: json['newPostsToday'] ?? 0,
    );
  }
}

class AdminUserStats {
  final int postsCount;
  final int commentsCount;
  final int followersCount;
  final int followingCount;
  final int friendsCount;
  final int reelsCount;

  AdminUserStats({
    required this.postsCount,
    required this.commentsCount,
    required this.followersCount,
    required this.followingCount,
    required this.friendsCount,
    required this.reelsCount,
  });

  factory AdminUserStats.fromJson(Map<String, dynamic> json) {
    return AdminUserStats(
      postsCount: json['postsCount'] ?? 0,
      commentsCount: json['commentsCount'] ?? 0,
      followersCount: json['followersCount'] ?? 0,
      followingCount: json['followingCount'] ?? 0,
      friendsCount: json['friendsCount'] ?? 0,
      reelsCount: json['reelsCount'] ?? 0,
    );
  }
}

class AdminCommentDetail {
  final int id;
  final String content;
  final int postId;
  final AdminCommentUser user;
  final String createdAt;
  final bool isReported;

  AdminCommentDetail({
    required this.id,
    required this.content,
    required this.postId,
    required this.user,
    required this.createdAt,
    required this.isReported,
  });

  factory AdminCommentDetail.fromJson(Map<String, dynamic> json) {
    return AdminCommentDetail(
      id: json['id'] ?? 0,
      content: json['content'] ?? '',
      postId: json['postId'] ?? 0,
      user: AdminCommentUser.fromJson(json['user'] ?? {}),
      createdAt: json['createdAt'] ?? '',
      isReported: json['isReported'] ?? false,
    );
  }
}

class AdminCommentUser {
  final int id;
  final String username;
  final String? avatarUrl;

  AdminCommentUser({
    required this.id,
    required this.username,
    this.avatarUrl,
  });

  factory AdminCommentUser.fromJson(Map<String, dynamic> json) {
    return AdminCommentUser(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      avatarUrl: json['avatarUrl'],
    );
  }
}

class TopUser {
  final int id;
  final String username;
  final String? avatarUrl;
  final bool isVerified;
  final int metric; // followers, posts, etc.

  TopUser({
    required this.id,
    required this.username,
    this.avatarUrl,
    required this.isVerified,
    required this.metric,
  });

  factory TopUser.fromJson(Map<String, dynamic> json) {
    return TopUser(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      avatarUrl: json['avatarUrl'],
      isVerified: json['isVerified'] ?? false,
      metric: json['metric'] ?? 0,
    );
  }
}
