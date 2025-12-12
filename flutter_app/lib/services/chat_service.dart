import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';
import '../models/chat_models.dart';
import '../models/error_models.dart';

class ChatService {
  final String baseUrl = ApiConfig.baseUrl;
  
  /// Lấy danh sách conversations
  Future<List<Conversation>> getConversations() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/messages/conversations'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      if (data['success'] == true) {
        return (data['data'] as List)
            .map((c) => Conversation.fromJson(c))
            .toList();
      } else {
        throw Exception(data['message'] ?? 'Failed to load conversations');
      }
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }
  
  /// Lấy chat history với một user
  Future<ChatHistory> getChatHistory(int userId, {int page = 0, int size = 50}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/messages/chat/$userId?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      if (data['success'] == true) {
        return ChatHistory.fromJson(data['data']);
      } else {
        throw Exception(data['message'] ?? 'Failed to load chat history');
      }
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
      Uri.parse('$baseUrl/api/messages'),
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
    
    if (response.statusCode == 201 || response.statusCode == 200) {
      final data = json.decode(response.body);
      if (data['success'] == true) {
        return ChatMessage.fromJson(data['data']);
      } else {
        throw Exception(data['message'] ?? 'Failed to send message');
      }
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }
  
  /// Đánh dấu messages là đã đọc
  Future<void> markAsRead(int userId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) return;
    
    await http.put(
      Uri.parse('$baseUrl/api/messages/read/$userId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
  }
  
  // Note: WebSocket connection is handled directly in chat_screen.dart using STOMP
  // sendMessage() is kept as REST API fallback when WebSocket is disconnected
}
