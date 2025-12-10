import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';

class AdminService {
  static const String baseUrl = ApiConfig.baseUrl;

  /// Get access token from storage
  static Future<String?> _getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('access_token');
  }

  // ==================== USER MANAGEMENT ====================

  /// Get all users (admin)
  static Future<Map<String, dynamic>> getAllUsers({
    int page = 0,
    int size = 20,
    String? search,
    String? role,
    String? status,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var queryParams = 'page=$page&size=$size';
    if (search != null) queryParams += '&search=$search';
    if (role != null) queryParams += '&role=$role';
    if (status != null) queryParams += '&status=$status';

    final response = await http.get(
      Uri.parse('$baseUrl/api/admin/users?$queryParams'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load users');
    }
  }

  /// Get user by ID (admin)
  static Future<Map<String, dynamic>> getUserById(int userId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/admin/users/$userId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load user');
    }
  }

  /// Ban user
  static Future<bool> banUser(int userId, String reason) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('$baseUrl/api/admin/users/$userId/ban'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'reason': reason}),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to ban user');
    }
  }

  /// Unban user
  static Future<bool> unbanUser(int userId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('$baseUrl/api/admin/users/$userId/unban'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to unban user');
    }
  }

  /// Verify user
  static Future<bool> verifyUser(int userId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('$baseUrl/api/admin/users/$userId/verify'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to verify user');
    }
  }

  /// Unverify user
  static Future<bool> unverifyUser(int userId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('$baseUrl/api/admin/users/$userId/unverify'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to unverify user');
    }
  }

  /// Delete user
  static Future<bool> deleteUser(int userId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/api/admin/users/$userId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to delete user');
    }
  }

  /// Get user statistics
  static Future<Map<String, dynamic>> getUserStats(int userId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/admin/users/$userId/stats'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load user stats');
    }
  }

  // ==================== POST MANAGEMENT ====================

  /// Search posts (admin)
  static Future<Map<String, dynamic>> searchPosts({
    int page = 0,
    int size = 20,
    String? keyword,
    int? userId,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var queryParams = 'page=$page&size=$size';
    if (keyword != null) queryParams += '&keyword=$keyword';
    if (userId != null) queryParams += '&userId=$userId';

    final response = await http.get(
      Uri.parse('$baseUrl/api/admin/posts/search?$queryParams'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to search posts');
    }
  }

  /// Delete post (admin)
  static Future<bool> deletePost(int postId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/api/admin/posts/$postId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to delete post');
    }
  }

  /// Hide post
  static Future<bool> hidePost(int postId, String reason) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('$baseUrl/api/admin/posts/$postId/hide'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'reason': reason}),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to hide post');
    }
  }

  /// Unhide post
  static Future<bool> unhidePost(int postId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('$baseUrl/api/admin/posts/$postId/unhide'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to unhide post');
    }
  }

  // ==================== COMMENT MANAGEMENT ====================

  /// Get all comments (admin)
  static Future<Map<String, dynamic>> getAllComments({
    int page = 0,
    int size = 20,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/admin/comments?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load comments');
    }
  }

  /// Get comments by post (admin)
  static Future<Map<String, dynamic>> getCommentsByPost(
    int postId, {
    int page = 0,
    int size = 20,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/admin/comments/post/$postId?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load post comments');
    }
  }

  /// Get comments by user (admin)
  static Future<Map<String, dynamic>> getCommentsByUser(
    int userId, {
    int page = 0,
    int size = 20,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/admin/comments/user/$userId?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load user comments');
    }
  }

  /// Delete comment (admin)
  static Future<bool> deleteComment(int commentId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/api/admin/comments/$commentId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to delete comment');
    }
  }

  /// Bulk delete comments
  static Future<Map<String, dynamic>> bulkDeleteComments(List<int> commentIds) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/api/admin/comments/bulk'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'commentIds': commentIds}),
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to bulk delete comments');
    }
  }

  // ==================== STATISTICS ====================

  /// Get overview statistics
  static Future<Map<String, dynamic>> getOverviewStats({String? period}) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var url = '$baseUrl/api/admin/stats/overview';
    if (period != null) url += '?period=$period';

    final response = await http.get(
      Uri.parse(url),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load overview stats');
    }
  }

  /// Get user statistics
  static Future<Map<String, dynamic>> getStatsUsers({String? period}) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var url = '$baseUrl/api/admin/stats/users';
    if (period != null) url += '?period=$period';

    final response = await http.get(
      Uri.parse(url),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load user stats');
    }
  }

  /// Get post statistics
  static Future<Map<String, dynamic>> getStatsPosts({String? period}) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var url = '$baseUrl/api/admin/stats/posts';
    if (period != null) url += '?period=$period';

    final response = await http.get(
      Uri.parse(url),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load post stats');
    }
  }

  /// Get engagement statistics
  static Future<Map<String, dynamic>> getStatsEngagement({String? period}) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var url = '$baseUrl/api/admin/stats/engagement';
    if (period != null) url += '?period=$period';

    final response = await http.get(
      Uri.parse(url),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load engagement stats');
    }
  }

  /// Get top users
  static Future<List<Map<String, dynamic>>> getTopUsers({
    String metric = 'followers',
    int limit = 10,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/admin/stats/top-users?metric=$metric&limit=$limit'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return List<Map<String, dynamic>>.from(data['topUsers'] ?? []);
    } else {
      throw Exception('Failed to load top users');
    }
  }

  /// Get top posts
  static Future<List<Map<String, dynamic>>> getTopPosts({
    String metric = 'likes',
    int limit = 10,
    String? period,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var url = '$baseUrl/api/admin/stats/top-posts?metric=$metric&limit=$limit';
    if (period != null) url += '&period=$period';

    final response = await http.get(
      Uri.parse(url),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return List<Map<String, dynamic>>.from(data['topPosts'] ?? []);
    } else {
      throw Exception('Failed to load top posts');
    }
  }
}
