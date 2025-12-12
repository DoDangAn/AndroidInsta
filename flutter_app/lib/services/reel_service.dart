import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';

class ReelService {
  static const String baseUrl = '${ApiConfig.baseUrl}/api/reels';

  /// Get access token from storage
  static Future<String?> _getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('access_token');
  }

  /// Upload reel video with metadata
  static Future<Map<String, dynamic>> uploadReel({
    required File videoFile,
    String caption = '',
    String visibility = 'PUBLIC',
    String quality = 'HIGH',
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    var request = http.MultipartRequest('POST', Uri.parse('$baseUrl/upload'));
    request.headers['Authorization'] = 'Bearer $token';
    
    request.files.add(await http.MultipartFile.fromPath('video', videoFile.path));
    if (caption.isNotEmpty) request.fields['caption'] = caption;
    request.fields['visibility'] = visibility;
    request.fields['quality'] = quality;

    final response = await request.send();
    final responseData = await response.stream.bytesToString();
    
    if (response.statusCode == 200) {
      return jsonDecode(responseData);
    } else {
      final error = jsonDecode(responseData);
      throw Exception(error['message'] ?? 'Failed to upload reel');
    }
  }

  /// Get reel by ID
  static Future<Map<String, dynamic>> getReelById(int reelId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$reelId'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load reel');
    }
  }

  /// Delete reel
  static Future<void> deleteReel(int reelId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/$reelId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to delete reel');
    }
  }

  /// Get feed of reels
  static Future<List<Map<String, dynamic>>> getReelsFeed({
    int page = 0,
    int size = 20,
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/feed?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return List<Map<String, dynamic>>.from(data['reels'] ?? []);
    } else {
      throw Exception('Failed to load reels feed');
    }
  }

  /// Like a reel
  static Future<bool> likeReel(int reelId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/$reelId/like'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to like reel');
    }
  }

  /// Unlike a reel
  static Future<bool> unlikeReel(int reelId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/$reelId/like'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to unlike reel');
    }
  }

  /// Add comment to reel
  static Future<Map<String, dynamic>> addComment(int reelId, String content) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/$reelId/comments'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'content': content}),
    );

    if (response.statusCode == 201) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to add comment');
    }
  }

  /// Get reel comments
  static Future<List<Map<String, dynamic>>> getComments(int reelId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$reelId/comments'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return List<Map<String, dynamic>>.from(data['comments'] ?? []);
    } else {
      throw Exception('Failed to load comments');
    }
  }
}
