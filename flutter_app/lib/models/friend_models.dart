// Friend models
class FriendRequest {
  final int id;
  final int senderId;
  final String senderUsername;
  final String? senderFullName;
  final String? senderAvatarUrl;
  final int receiverId;
  final String receiverUsername;
  final String? receiverFullName;
  final String? receiverAvatarUrl;
  final String status;
  final String createdAt;
  final String? respondedAt;

  FriendRequest({
    required this.id,
    required this.senderId,
    required this.senderUsername,
    this.senderFullName,
    this.senderAvatarUrl,
    required this.receiverId,
    required this.receiverUsername,
    this.receiverFullName,
    this.receiverAvatarUrl,
    required this.status,
    required this.createdAt,
    this.respondedAt,
  });

  factory FriendRequest.fromJson(Map<String, dynamic> json) {
    return FriendRequest(
      id: json['id'],
      senderId: json['senderId'],
      senderUsername: json['senderUsername'],
      senderFullName: json['senderFullName'],
      senderAvatarUrl: json['senderAvatarUrl'],
      receiverId: json['receiverId'],
      receiverUsername: json['receiverUsername'],
      receiverFullName: json['receiverFullName'],
      receiverAvatarUrl: json['receiverAvatarUrl'],
      status: json['status'],
      createdAt: json['createdAt'],
      respondedAt: json['respondedAt'],
    );
  }
}

class Friend {
  final int id;
  final int userId;
  final String username;
  final String? fullName;
  final String? avatarUrl;
  final String? bio;
  final int mutualFriendsCount;
  final String friendsSince;

  Friend({
    required this.id,
    required this.userId,
    required this.username,
    this.fullName,
    this.avatarUrl,
    this.bio,
    required this.mutualFriendsCount,
    required this.friendsSince,
  });

  factory Friend.fromJson(Map<String, dynamic> json) {
    return Friend(
      id: json['id'],
      userId: json['userId'],
      username: json['username'],
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
      bio: json['bio'],
      mutualFriendsCount: json['mutualFriendsCount'] ?? 0,
      friendsSince: json['friendsSince'],
    );
  }
}

class FriendSuggestion {
  final int userId;
  final String username;
  final String? fullName;
  final String? avatarUrl;
  final String? bio;
  final int mutualFriendsCount;

  FriendSuggestion({
    required this.userId,
    required this.username,
    this.fullName,
    this.avatarUrl,
    this.bio,
    required this.mutualFriendsCount,
  });

  factory FriendSuggestion.fromJson(Map<String, dynamic> json) {
    return FriendSuggestion(
      userId: json['userId'],
      username: json['username'],
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
      bio: json['bio'],
      mutualFriendsCount: json['mutualFriendsCount'] ?? 0,
    );
  }
}

class FriendshipStatus {
  final bool isFriend;
  final bool hasPendingRequest;
  final bool pendingRequestSentByMe;
  final int? friendRequestId;

  FriendshipStatus({
    required this.isFriend,
    required this.hasPendingRequest,
    required this.pendingRequestSentByMe,
    this.friendRequestId,
  });

  factory FriendshipStatus.fromJson(Map<String, dynamic> json) {
    return FriendshipStatus(
      isFriend: json['isFriend'] ?? false,
      hasPendingRequest: json['hasPendingRequest'] ?? false,
      pendingRequestSentByMe: json['pendingRequestSentByMe'] ?? false,
      friendRequestId: json['friendRequestId'],
    );
  }
}
