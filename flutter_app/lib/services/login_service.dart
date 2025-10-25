import 'dart:convert';
import 'package:http/http.dart' as http;

// Demo Login Service cho Flutter App
class LoginService {
  static const String baseUrl = 'http://10.0.2.2:8081/api/auth';
  
  // Login method
  static Future<LoginResponse> login(String usernameOrEmail, String password) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/login'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'usernameOrEmail': usernameOrEmail,
          'password': password,
        }),
      );
      
      final data = jsonDecode(response.body);
      
      if (response.statusCode == 200 && data['success']) {
        return LoginResponse.fromJson(data);
      } else {
        throw Exception(data['message'] ?? 'Login failed');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }
  
  // Register method
  static Future<LoginResponse> register(String username, String email, String password, {String? fullName}) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/register'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'username': username,
          'email': email,
          'password': password,
          'fullName': fullName,
        }),
      );
      
      final data = jsonDecode(response.body);
      
      if (response.statusCode == 201 && data['success']) {
        return LoginResponse.fromJson(data);
      } else {
        throw Exception(data['message'] ?? 'Registration failed');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }
  
  // Get current user
  static Future<Map<String, dynamic>> getCurrentUser(String token) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/me'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );
      
      final data = jsonDecode(response.body);
      
      if (response.statusCode == 200 && data['success']) {
        return data;
      } else {
        throw Exception(data['message'] ?? 'Failed to get user info');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }
  
  // Logout
  static Future<void> logout(String token) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/logout'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );
      
      final data = jsonDecode(response.body);
      
      if (response.statusCode != 200 || !data['success']) {
        throw Exception(data['message'] ?? 'Logout failed');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }
}

// Data models
class LoginResponse {
  final bool success;
  final String message;
  final LoginData? data;
  
  LoginResponse({
    required this.success,
    required this.message,
    this.data,
  });
  
  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      success: json['success'] ?? false,
      message: json['message'] ?? '',
      data: json['data'] != null ? LoginData.fromJson(json['data']) : null,
    );
  }
}

class LoginData {
  final String accessToken;
  final String refreshToken;
  final String tokenType;
  final int expiresIn;
  final UserInfo user;
  
  LoginData({
    required this.accessToken,
    required this.refreshToken,
    required this.tokenType,
    required this.expiresIn,
    required this.user,
  });
  
  factory LoginData.fromJson(Map<String, dynamic> json) {
    return LoginData(
      accessToken: json['accessToken'] ?? '',
      refreshToken: json['refreshToken'] ?? '',
      tokenType: json['tokenType'] ?? 'Bearer',
      expiresIn: json['expiresIn'] ?? 0,
      user: UserInfo.fromJson(json['user']),
    );
  }
}

class UserInfo {
  final int id;
  final String username;
  final String email;
  final List<String> roles;
  
  UserInfo({
    required this.id,
    required this.username,
    required this.email,
    required this.roles,
  });
  
  factory UserInfo.fromJson(Map<String, dynamic> json) {
    return UserInfo(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      email: json['email'] ?? '',
      roles: List<String>.from(json['roles'] ?? []),
    );
  }
}