import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class ChatService {
  final String baseUrl = 'http://10.0.2.2:8081';
  
  /// Lấy danh sách conversations
  Future<List<Conversation>> getConversations() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/chat/conversations'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return (data['conversations'] as List)
          .map((c) => Conversation.fromJson(c))
          .toList();
    } else {
      throw Exception('Failed to load conversations');
    }
  }
  
  /// Lấy chat history với một user
  Future<ChatHistory> getChatHistory(int userId, {int page = 0, int size = 50}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/chat/$userId?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      return ChatHistory.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to load chat history');
    }
  }
  
  /// Gửi message qua REST API
  Future<ChatMessage> sendMessage(int receiverId, String content, {String? mediaUrl}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.post(
      Uri.parse('$baseUrl/api/chat/send'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: json.encode({
        'receiverId': receiverId,
        'content': content,
        'mediaUrl': mediaUrl,
        'messageType': mediaUrl != null ? 'IMAGE' : 'TEXT',
      }),
    );
    
    if (response.statusCode == 201) {
      return ChatMessage.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to send message');
    }
  }
  
  /// Đánh dấu messages là đã đọc
  Future<void> markAsRead(int userId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) return;
    
    await http.put(
      Uri.parse('$baseUrl/api/chat/read/$userId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
  }
  
  /// Connect WebSocket
  WebSocketChannel connectWebSocket(String token) {
    const wsUrl = 'ws://10.0.2.2:8081/ws';
    return WebSocketChannel.connect(Uri.parse(wsUrl));
  }
}

// Models
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
    return ChatMessage(
      id: json['id'],
      content: json['content'],
      mediaUrl: json['mediaUrl'],
      messageType: json['messageType'],
      sender: UserSummary.fromJson(json['sender']),
      receiver: UserSummary.fromJson(json['receiver']),
      isRead: json['isRead'],
      createdAt: json['createdAt'],
    );
  }
}

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
      id: json['id'],
      username: json['username'],
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
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
