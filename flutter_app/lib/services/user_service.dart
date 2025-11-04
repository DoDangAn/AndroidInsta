import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';
import '../models/user_models.dart';

class UserService {
  static const String baseUrl = ApiConfig.usersUrl;

  /// Get access token from storage
  static Future<String?> _getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('access_token');
  }

  /// Get current user profile
  static Future<UserProfile> getCurrentProfile() async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/profile'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserProfile.fromJson(data['data']);
    } else {
      throw Exception('Failed to load profile');
    }
  }

  /// Get user by ID
  static Future<UserProfile> getUserById(int userId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$userId'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserProfile.fromJson(data['data']);
    } else {
      throw Exception('Failed to load user');
    }
  }

  /// Search users
  static Future<List<UserProfile>> searchUsers(String keyword) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/search?keyword=$keyword'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return (data['data'] as List)
          .map((u) => UserProfile.fromJson(u))
          .toList();
    } else {
      throw Exception('Failed to search users');
    }
  }

  /// Follow a user
  static Future<bool> followUser(int userId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/$userId/follow'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to follow user');
    }
  }

  /// Unfollow a user
  static Future<bool> unfollowUser(int userId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/$userId/follow'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to unfollow user');
    }
  }

  /// Get user stats (followers, following, posts count)
  static Future<UserStats> getUserStats(int userId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$userId/stats'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserStats.fromJson(data['stats']);
    } else {
      throw Exception('Failed to load user stats');
    }
  }
}
