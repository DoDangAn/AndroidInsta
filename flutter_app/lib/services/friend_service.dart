import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';
import '../models/friend_models.dart';
import '../models/error_models.dart';

class FriendService {
  final String baseUrl = ApiConfig.baseUrl;

  /// Gửi friend request
  Future<FriendRequest> sendFriendRequest(int receiverId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.post(
      Uri.parse('$baseUrl/api/friends/requests'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: json.encode({'receiverId': receiverId}),
    );
    
    if (response.statusCode == 201 || response.statusCode == 200) {
      return FriendRequest.fromJson(json.decode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Lấy danh sách friend requests đã nhận
  Future<List<FriendRequest>> getReceivedFriendRequests({int page = 0, int size = 20}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/friends/requests/received?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return (data['content'] as List)
          .map((r) => FriendRequest.fromJson(r))
          .toList();
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Lấy danh sách friend requests đã gửi
  Future<List<FriendRequest>> getSentFriendRequests({int page = 0, int size = 20}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/friends/requests/sent?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return (data['content'] as List)
          .map((r) => FriendRequest.fromJson(r))
          .toList();
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  /// Đếm pending requests
  Future<int> getPendingRequestsCount() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/friends/requests/count'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return data['count'] ?? 0;
    } else {
      return 0;
    }
  }

  /// Chấp nhận friend request
  Future<FriendRequest> acceptFriendRequest(int requestId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.put(
      Uri.parse('$baseUrl/api/friends/requests/$requestId/accept'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      return FriendRequest.fromJson(json.decode(response.body));
    } else {
      final error = json.decode(response.body);
      throw Exception(error['message'] ?? 'Failed to accept request');
    }
  }

  /// Từ chối friend request
  Future<FriendRequest> rejectFriendRequest(int requestId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.put(
      Uri.parse('$baseUrl/api/friends/requests/$requestId/reject'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      return FriendRequest.fromJson(json.decode(response.body));
    } else {
      final error = json.decode(response.body);
      throw Exception(error['message'] ?? 'Failed to reject request');
    }
  }

  /// Hủy friend request đã gửi
  Future<void> cancelFriendRequest(int requestId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.delete(
      Uri.parse('$baseUrl/api/friends/requests/$requestId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode != 200) {
      final error = json.decode(response.body);
      throw Exception(error['message'] ?? 'Failed to cancel request');
    }
  }

  /// Lấy danh sách bạn bè
  Future<List<Friend>> getFriends({int page = 0, int size = 20}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/friends?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return (data['content'] as List)
          .map((f) => Friend.fromJson(f))
          .toList();
    } else {
      throw Exception('Failed to load friends');
    }
  }

  /// Đếm số bạn bè
  Future<int> getFriendsCount() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/friends/count'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return data['count'] ?? 0;
    } else {
      return 0;
    }
  }

  /// Unfriend
  Future<void> unfriend(int friendId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.delete(
      Uri.parse('$baseUrl/api/friends/$friendId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode != 200) {
      final error = json.decode(response.body);
      throw Exception(error['message'] ?? 'Failed to unfriend');
    }
  }

  /// Lấy friend suggestions
  Future<List<FriendSuggestion>> getFriendSuggestions({int page = 0, int size = 10}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/friends/suggestions?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return (data['content'] as List)
          .map((s) => FriendSuggestion.fromJson(s))
          .toList();
    } else {
      throw Exception('Failed to load suggestions');
    }
  }

  /// Kiểm tra friendship status
  Future<FriendshipStatus> getFriendshipStatus(int userId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) throw Exception('Not authenticated');
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/friends/status/$userId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      return FriendshipStatus.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to get friendship status');
    }
  }
}
