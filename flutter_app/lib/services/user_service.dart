import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';
import '../models/user_models.dart';
import '../models/error_models.dart';

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
      return UserProfile.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return UserProfile.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return (data as List).map((u) => UserProfile.fromJson(u)).toList();
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return data['isFollowing'] ?? true;
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return data['isFollowing'] ?? false;
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return UserStats.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return (data as List).map((u) => UserProfile.fromJson(u)).toList();
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return (data as List).map((u) => UserProfile.fromJson(u)).toList();
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Get follow status (isFollowing, isFollower)
  static Future<Map<String, bool>> getFollowStatus(int userId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$userId/follow-status'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return {
        'isFollowing': data['isFollowing'] ?? false,
        'isFollower': data['isFollower'] ?? false,
      };
    } else {
      return {'isFollowing': false, 'isFollower': false};
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
      return UserProfile.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Upload avatar
  static Future<String> uploadAvatar(String imagePath) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var request = http.MultipartRequest('POST', Uri.parse('$baseUrl/avatar'));
    request.headers['Authorization'] = 'Bearer $token';
    
    request.files.add(await http.MultipartFile.fromPath('file', imagePath));

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['avatarUrl'];
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }
}
