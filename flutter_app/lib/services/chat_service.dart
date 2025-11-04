import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import '../config/api_config.dart';
import '../models/chat_models.dart';

class ChatService {
  final String baseUrl = ApiConfig.baseUrl;
  
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
