import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/notification_models.dart';
import '../config/api_config.dart';

class NotificationService {
  final String baseUrl = ApiConfig.baseUrl;

  Future<List<NotificationModel>> getNotifications({int page = 0, int size = 20}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/notifications?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      if (data['content'] != null) {
        return (data['content'] as List)
            .map((e) => NotificationModel.fromJson(e))
            .toList();
      }
      return [];
    } else {
      throw Exception('Failed to load notifications');
    }
  }

  Future<int> getUnreadCount() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) return 0;

    final response = await http.get(
      Uri.parse('$baseUrl/api/notifications/unread/count'),
      headers: {
        'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return data['count'] ?? 0;
    }
    return 0;
  }

  Future<void> markAsRead(int id) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) return;

    await http.put(
      Uri.parse('$baseUrl/api/notifications/$id/read'),
      headers: {
        'Authorization': 'Bearer $token',
      },
    );
  }
  
  Future<void> markAllAsRead() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) return;

    await http.put(
      Uri.parse('$baseUrl/api/notifications/read-all'),
      headers: {
        'Authorization': 'Bearer $token',
      },
    );
  }
}
