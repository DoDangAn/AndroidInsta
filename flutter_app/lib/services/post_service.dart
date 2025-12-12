import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';
import '../models/post_models.dart';
import '../models/error_models.dart';

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
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Get recent PUBLIC posts (last 7 days)
  /// Used for Explore/Discover feature
  static Future<FeedResponse> getAdvertisePosts({int page = 0, int size = 20}) async {
    final token = await _getToken();
    
    final response = await http.get(
      Uri.parse('$baseUrl/advertise?page=$page&size=$size'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return FeedResponse.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Get a single post by ID
  static Future<PostDto> getPostById(int postId) async {
    final token = await _getToken();

    final response = await http.get(
      Uri.parse('$baseUrl/$postId'),
      headers: {
        if (token != null) 'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return PostDto.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Update a post
  static Future<PostDto> updatePost(int postId, {String? caption, String? visibility}) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('$baseUrl/$postId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'caption': caption,
        'visibility': visibility,
      }),
    );

    if (response.statusCode == 200) {
      return PostDto.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return data['isLiked'] ?? true;
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      return data['isLiked'] ?? false;
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Add comment to post (or reply to comment)
  static Future<Comment> addComment(int postId, String content, {int? parentCommentId}) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final requestBody = {
      'content': content,
      if (parentCommentId != null) 'parentCommentId': parentCommentId,
    };

    final response = await http.post(
      Uri.parse('$baseUrl/$postId/comments'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode(requestBody),
    );

    if (response.statusCode == 201) {
      return Comment.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
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
      if (data is List) {
        return data.map((c) => Comment.fromJson(c)).toList();
      } else {
        throw Exception('Invalid response format');
      }
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Delete a comment
  static Future<void> deleteComment(int postId, int commentId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('$baseUrl/$postId/comments/$commentId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode != 204) {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Upload a post with image
  static Future<Map<String, dynamic>> uploadPost({
    required String imagePath,
    String? caption,
    String visibility = 'PUBLIC',
    String quality = 'HIGH',
  }) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    try {
      final uri = Uri.parse('$baseUrl/upload-single');
      final request = http.MultipartRequest('POST', uri);

      // Add headers
      request.headers['Authorization'] = 'Bearer $token';

      // Add image file with proper content type
      final imageFile = File(imagePath);

      // Check if file exists
      if (!await imageFile.exists()) {
        throw Exception('Image file not found at path: $imagePath');
      }

      final stream = http.ByteStream(imageFile.openRead());
      final length = await imageFile.length();

      print('Uploading image: ${imagePath.split('/').last}');
      print('File size: $length bytes');

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

      print('Content type: $contentType');

      final multipartFile = http.MultipartFile(
        'image',
        stream,
        length,
        filename: imagePath.split('/').last,
        contentType: MediaType.parse(contentType),
      );
      request.files.add(multipartFile);

      // Add form fields
      if (caption != null && caption.isNotEmpty) {
        request.fields['caption'] = caption;
      }
      request.fields['visibility'] = visibility;
      request.fields['quality'] = quality;

      print('Sending request to: $uri');
      print('Fields: ${request.fields}');

      // Send request
      final streamedResponse = await request.send();
      final response = await http.Response.fromStream(streamedResponse);

      print('Response status: ${response.statusCode}');
      print('Response body: ${response.body}');

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        throw ApiErrorParser.parseError(response.statusCode, response.body);
      }
    } catch (e) {
      rethrow;
    }
  }
}
