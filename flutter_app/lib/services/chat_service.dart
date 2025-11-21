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
      throw Exception('Failed to load conversations');
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
    
    if (token == null) throw Exception('Không xác thực được. Vui lòng đăng nhập lại.');
    
    try {
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
      
      print('Send message response status: ${response.statusCode}');
      print('Send message response body: ${response.body}');
      
      if (response.statusCode == 201 || response.statusCode == 200) {
        try {
          final data = json.decode(response.body);
          if (data['success'] == true) {
            return ChatMessage.fromJson(data['data']);
          } else {
            final errorMessage = data['message'] ?? 'Không thể gửi tin nhắn';
            print('Error from server: $errorMessage');
            throw Exception(errorMessage);
          }
        } catch (e) {
          if (e is Exception) rethrow;
          print('Error parsing response: $e');
          throw Exception('Lỗi khi xử lý phản hồi từ server: ${response.body}');
        }
      } else if (response.statusCode == 401) {
        throw Exception('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
      } else if (response.statusCode == 400) {
        try {
          final data = json.decode(response.body);
          final errorMessage = data['message'] ?? 'Dữ liệu không hợp lệ';
          throw Exception(errorMessage);
        } catch (e) {
          throw Exception('Dữ liệu không hợp lệ: ${response.body}');
        }
      } else {
        throw Exception('Lỗi server (${response.statusCode}): ${response.body}');
      }
    } catch (e) {
      if (e is Exception) {
        rethrow;
      }
      print('Network error: $e');
      throw Exception('Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.');
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
  
  /// Connect WebSocket
  WebSocketChannel connectWebSocket(String token) {
    const wsUrl = 'ws://10.0.2.2:8081/ws';
    return WebSocketChannel.connect(Uri.parse(wsUrl));
  }
}
