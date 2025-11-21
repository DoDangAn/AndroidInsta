class NotificationModel {
  final int id;
  final int senderId;
  final String senderUsername;
  final String? senderAvatarUrl;
  final String type; // LIKE, COMMENT, FOLLOW
  final String? message;
  final int? entityId;
  final bool isRead;
  final String createdAt;

  NotificationModel({
    required this.id,
    required this.senderId,
    required this.senderUsername,
    this.senderAvatarUrl,
    required this.type,
    this.message,
    this.entityId,
    required this.isRead,
    required this.createdAt,
  });

  factory NotificationModel.fromJson(Map<String, dynamic> json) {
    return NotificationModel(
      id: json['id'],
      senderId: json['senderId'],
      senderUsername: json['senderUsername'],
      senderAvatarUrl: json['senderAvatarUrl'],
      type: json['type'],
      message: json['message'],
      entityId: json['entityId'],
      isRead: json['isRead'],
      createdAt: json['createdAt'],
    );
  }
}
