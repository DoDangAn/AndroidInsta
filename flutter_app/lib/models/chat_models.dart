// UserSummary for chat
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

class Conversation {
  final UserSummary user;
  final ChatMessage? lastMessage;
  final int unreadCount;
  
  Conversation({
    required this.user,
    this.lastMessage,
    required this.unreadCount,
  });
  
  factory Conversation.fromJson(Map<String, dynamic> json) {
    return Conversation(
      user: UserSummary.fromJson(json['user']),
      lastMessage: json['lastMessage'] != null 
          ? ChatMessage.fromJson(json['lastMessage'])
          : null,
      unreadCount: json['unreadCount'] ?? 0,
    );
  }
}

class ChatMessage {
  final int id;
  final String? content;
  final String? mediaUrl;
  final String messageType;
  final UserSummary sender;
  final UserSummary receiver;
  final bool isRead;
  final String createdAt;
  
  ChatMessage({
    required this.id,
    this.content,
    this.mediaUrl,
    required this.messageType,
    required this.sender,
    required this.receiver,
    required this.isRead,
    required this.createdAt,
  });
  
  factory ChatMessage.fromJson(Map<String, dynamic> json) {
    // Handle createdAt - can be String or DateTime object
    String createdAtStr;
    if (json['createdAt'] is String) {
      createdAtStr = json['createdAt'];
    } else if (json['createdAt'] != null) {
      // If it's a DateTime object, convert to ISO string
      createdAtStr = json['createdAt'].toString();
    } else {
      createdAtStr = DateTime.now().toIso8601String();
    }
    
    return ChatMessage(
      id: json['id'] ?? 0,
      content: json['content'],
      mediaUrl: json['mediaUrl'],
      messageType: json['messageType'] ?? 'TEXT',
      sender: UserSummary.fromJson(json['sender'] ?? {}),
      receiver: UserSummary.fromJson(json['receiver'] ?? {}),
      isRead: json['isRead'] ?? false,
      createdAt: createdAtStr,
    );
  }
}

class ChatHistory {
  final List<ChatMessage> messages;
  final int currentPage;
  final int totalPages;
  final int totalItems;
  
  ChatHistory({
    required this.messages,
    required this.currentPage,
    required this.totalPages,
    required this.totalItems,
  });
  
  factory ChatHistory.fromJson(Map<String, dynamic> json) {
    return ChatHistory(
      messages: (json['messages'] as List)
          .map((m) => ChatMessage.fromJson(m))
          .toList(),
      currentPage: json['currentPage'],
      totalPages: json['totalPages'],
      totalItems: json['totalItems'],
    );
  }
}

