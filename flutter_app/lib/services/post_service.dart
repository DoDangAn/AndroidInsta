import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';
import '../models/post_models.dart';

class PostService {
  static const String baseUrl = ApiConfig.postsUrl;

  /// Get access token from storage
  static Future<String?> _getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('access_token');
  }

  /// Get feed posts
  static Future<FeedResponse> getFeed({int page = 0, int size = 20}) async {
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
      return FeedResponse.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to load feed');
    }
  }

  /// Get user posts
  static Future<FeedResponse> getUserPosts(int userId, {int page = 0, int size = 20}) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/user/$userId?page=$page&size=$size'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return FeedResponse.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to load user posts');
    }
  }

  /// Create a new post
  static Future<PostDto> createPost({
    required String caption,
    String visibility = 'PUBLIC',
    List<String> mediaUrls = const [],
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse(baseUrl),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'caption': caption,
        'visibility': visibility,
        'mediaUrls': mediaUrls,
      }),
    );

    if (response.statusCode == 201) {
      return PostDto.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to create post');
    }
  }

  /// Delete a post
  static Future<void> deletePost(int postId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/$postId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode != 204) {
      throw Exception('Failed to delete post');
    }
  }

  /// Like a post
  static Future<bool> likePost(int postId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/$postId/like'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to like post');
    }
  }

  /// Unlike a post
  static Future<bool> unlikePost(int postId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/$postId/like'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['success'] ?? false;
    } else {
      throw Exception('Failed to unlike post');
    }
  }

  /// Add comment to post
  static Future<Comment> addComment(int postId, String content) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/$postId/comments'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'content': content}),
    );

    if (response.statusCode == 201) {
      final data = jsonDecode(response.body);
      return Comment.fromJson(data['comment']);
    } else {
      throw Exception('Failed to add comment');
    }
  }

  /// Get post comments
  static Future<List<Comment>> getComments(int postId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$postId/comments'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return (data['comments'] as List)
          .map((c) => Comment.fromJson(c))
          .toList();
    } else {
      throw Exception('Failed to load comments');
    }
  }

  /// Delete a comment
  static Future<void> deleteComment(int commentId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/comments/$commentId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to delete comment');
    }
  }
}
