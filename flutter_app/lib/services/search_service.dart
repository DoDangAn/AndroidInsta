import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/search_models.dart';

class SearchService {
  static const String baseUrl = 'http://10.0.2.2:8080/api/search';

  // Get auth token
  Future<String?> _getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('auth_token');
  }

  // Get headers with auth token
  Future<Map<String, String>> _getHeaders() async {
    final token = await _getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  /// Search users
  Future<SearchPageResponse<UserSearchResult>> searchUsers({
    required String keyword,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/users?keyword=$keyword&page=$page&size=$size'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return SearchPageResponse.fromJson(
          json,
          (item) => UserSearchResult.fromJson(item),
        );
      } else {
        throw Exception('Failed to search users: ${response.statusCode}');
      }
    } catch (e) {
      print('Error searching users: $e');
      rethrow;
    }
  }

  /// Search posts
  Future<SearchPageResponse<PostSearchResult>> searchPosts({
    required String keyword,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/posts?keyword=$keyword&page=$page&size=$size'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return SearchPageResponse.fromJson(
          json,
          (item) => PostSearchResult.fromJson(item),
        );
      } else {
        throw Exception('Failed to search posts: ${response.statusCode}');
      }
    } catch (e) {
      print('Error searching posts: $e');
      rethrow;
    }
  }

  /// Search reels (video posts)
  Future<SearchPageResponse<PostSearchResult>> searchReels({
    required String keyword,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/reels?keyword=$keyword&page=$page&size=$size'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return SearchPageResponse.fromJson(
          json,
          (item) => PostSearchResult.fromJson(item),
        );
      } else {
        throw Exception('Failed to search reels: ${response.statusCode}');
      }
    } catch (e) {
      print('Error searching reels: $e');
      rethrow;
    }
  }

  /// Search tags
  Future<SearchPageResponse<TagSearchResult>> searchTags({
    required String keyword,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/tags?keyword=$keyword&page=$page&size=$size'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return SearchPageResponse.fromJson(
          json,
          (item) => TagSearchResult.fromJson(item),
        );
      } else {
        throw Exception('Failed to search tags: ${response.statusCode}');
      }
    } catch (e) {
      print('Error searching tags: $e');
      rethrow;
    }
  }

  /// Search all (users, posts, tags)
  Future<SearchAllResult> searchAll({required String keyword}) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/all?keyword=$keyword'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return SearchAllResult.fromJson(json);
      } else {
        throw Exception('Failed to search all: ${response.statusCode}');
      }
    } catch (e) {
      print('Error searching all: $e');
      rethrow;
    }
  }

  /// Get trending tags
  Future<SearchPageResponse<TagSearchResult>> getTrendingTags({
    int page = 0,
    int size = 20,
  }) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/trending/tags?page=$page&size=$size'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return SearchPageResponse.fromJson(
          json,
          (item) => TagSearchResult.fromJson(item),
        );
      } else {
        throw Exception('Failed to get trending tags: ${response.statusCode}');
      }
    } catch (e) {
      print('Error getting trending tags: $e');
      rethrow;
    }
  }

  /// Get search suggestions (autocomplete)
  Future<SearchSuggestions> getSearchSuggestions({
    required String query,
    int limit = 5,
  }) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/suggestions?q=$query&limit=$limit'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return SearchSuggestions.fromJson(json);
      } else {
        throw Exception('Failed to get suggestions: ${response.statusCode}');
      }
    } catch (e) {
      print('Error getting suggestions: $e');
      rethrow;
    }
  }
}
