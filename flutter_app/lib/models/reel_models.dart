class Reel {
  final int id;
  final ReelUser user;
  final String videoUrl;
  final String? thumbnailUrl;
  final String? caption;
  final String? musicName;
  final String? musicArtist;
  final int likesCount;
  final int commentsCount;
  final int viewsCount;
  final bool isLiked;
  final String createdAt;

  Reel({
    required this.id,
    required this.user,
    required this.videoUrl,
    this.thumbnailUrl,
    this.caption,
    this.musicName,
    this.musicArtist,
    required this.likesCount,
    required this.commentsCount,
    required this.viewsCount,
    required this.isLiked,
    required this.createdAt,
  });

  factory Reel.fromJson(Map<String, dynamic> json) {
    return Reel(
      id: json['id'] ?? 0,
      user: ReelUser.fromJson(json['user'] ?? {}),
      videoUrl: json['videoUrl'] ?? '',
      thumbnailUrl: json['thumbnailUrl'],
      caption: json['caption'],
      musicName: json['musicName'],
      musicArtist: json['musicArtist'],
      likesCount: json['likesCount'] ?? 0,
      commentsCount: json['commentsCount'] ?? 0,
      viewsCount: json['viewsCount'] ?? 0,
      isLiked: json['isLiked'] ?? false,
      createdAt: json['createdAt'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'user': user.toJson(),
      'videoUrl': videoUrl,
      'thumbnailUrl': thumbnailUrl,
      'caption': caption,
      'musicName': musicName,
      'musicArtist': musicArtist,
      'likesCount': likesCount,
      'commentsCount': commentsCount,
      'viewsCount': viewsCount,
      'isLiked': isLiked,
      'createdAt': createdAt,
    };
  }
}

class ReelUser {
  final int id;
  final String username;
  final String? fullName;
  final String? avatarUrl;
  final bool isVerified;

  ReelUser({
    required this.id,
    required this.username,
    this.fullName,
    this.avatarUrl,
    this.isVerified = false,
  });

  factory ReelUser.fromJson(Map<String, dynamic> json) {
    return ReelUser(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
      isVerified: json['isVerified'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'fullName': fullName,
      'avatarUrl': avatarUrl,
      'isVerified': isVerified,
    };
  }
}

class ReelComment {
  final int id;
  final String content;
  final ReelUser user;
  final String createdAt;

  ReelComment({
    required this.id,
    required this.content,
    required this.user,
    required this.createdAt,
  });

  factory ReelComment.fromJson(Map<String, dynamic> json) {
    return ReelComment(
      id: json['id'] ?? 0,
      content: json['content'] ?? '',
      user: ReelUser.fromJson(json['user'] ?? {}),
      createdAt: json['createdAt'] ?? '',
    );
  }
}
