import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart' as http_parser;
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

  /// Get followers list
  static Future<List<UserProfile>> getFollowers(int userId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$userId/followers'),
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
      throw Exception('Failed to load followers');
    }
  }

  /// Get following list
  static Future<List<UserProfile>> getFollowing(int userId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$userId/following'),
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
      throw Exception('Failed to load following');
    }
  }

  /// Update user profile
  static Future<UserProfile> updateProfile({
    String? fullName,
    String? bio,
    String? email,
    String? avatarUrl,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final body = <String, dynamic>{};
    if (fullName != null) body['fullName'] = fullName;
    if (bio != null) body['bio'] = bio;
    if (email != null) body['email'] = email;
    if (avatarUrl != null) body['avatarUrl'] = avatarUrl;

    final response = await http.put(
      Uri.parse('$baseUrl/profile'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode(body),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserProfile.fromJson(data['data']);
    } else {
      final error = jsonDecode(response.body);
      throw Exception(error['message'] ?? 'Failed to update profile');
    }
  }

  /// Upload avatar
  static Future<String> uploadAvatar(String imagePath) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    try {
      final uri = Uri.parse('${ApiConfig.postsUrl}/upload-avatar');
      final request = http.MultipartRequest('POST', uri);

      // Add headers
      request.headers['Authorization'] = 'Bearer $token';

      // Determine content type based on file extension
      String contentType = 'image/jpeg';
      final extension = imagePath.toLowerCase().split('.').last;
      if (extension == 'png') {
        contentType = 'image/png';
      } else if (extension == 'jpg' || extension == 'jpeg') {
        contentType = 'image/jpeg';
      } else if (extension == 'gif') {
        contentType = 'image/gif';
      } else if (extension == 'webp') {
        contentType = 'image/webp';
      }

      // Add image file with explicit content type
      final file = await http.MultipartFile.fromPath(
        'avatar',
        imagePath,
        contentType: http_parser.MediaType.parse(contentType),
      );
      request.files.add(file);

      // Send request
      final streamedResponse = await request.send();
      final response = await http.Response.fromStream(streamedResponse);

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['avatarUrl'] as String;
      } else {
        final error = jsonDecode(response.body);
        throw Exception(error['message'] ?? 'Failed to upload avatar');
      }
    } catch (e) {
      throw Exception('Failed to upload avatar: $e');
    }
  }
}
