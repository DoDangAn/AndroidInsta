import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/api_config.dart';
import '../models/user_models.dart';

class LoginResponse {
  final bool success;
  final String message;
  final AuthData? data;

  LoginResponse({
    required this.success,
    required this.message,
    this.data,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      success: json['success'] ?? false,
      message: json['message'] ?? '',
      data: json['data'] != null ? AuthData.fromJson(json['data']) : null,
    );
  }
}

class AuthData {
  final String accessToken;
  final String refreshToken;
  final UserProfile user;

  AuthData({
    required this.accessToken,
    required this.refreshToken,
    required this.user,
  });

  factory AuthData.fromJson(Map<String, dynamic> json) {
    return AuthData(
      accessToken: json['accessToken'] ?? '',
      refreshToken: json['refreshToken'] ?? '',
      user: UserProfile.fromJson(json['user']),
    );
  }
}

class LoginService {
  static const String baseUrl = ApiConfig.authUrl;

  static Future<LoginResponse> login(String username, String password) async {
    try {
      print('Attempting login for: $username');
      final response = await http.post(
        Uri.parse('$baseUrl/login'),
        headers: ApiConfig.headers,
        body: jsonEncode({
          'usernameOrEmail': username,
          'password': password,
        }),
      );

      print('Login response status: ${response.statusCode}');
      print('Login response body: ${response.body}');

      if (response.statusCode == 200) {
        return LoginResponse.fromJson(jsonDecode(response.body));
      } else {
        try {
          final error = jsonDecode(response.body);
          throw Exception(error['message'] ?? 'Login failed with status ${response.statusCode}');
        } catch (e) {
          if (e is FormatException) {
            throw Exception('Server returned invalid response: ${response.statusCode}');
          }
          rethrow;
        }
      }
    } catch (e) {
      print('Login error: $e');
      String message = e.toString().replaceAll('Exception: ', '');
      throw Exception(message);
    }
  }
  
  static Future<LoginResponse> googleLogin(String email, String googleId, {String? fullName, String? photoUrl}) async {
    try {
      print('Attempting Google login for: $email');
      final response = await http.post(
        Uri.parse('$baseUrl/google'),
        headers: ApiConfig.headers,
        body: jsonEncode({
          'email': email,
          'googleId': googleId,
          'fullName': fullName,
          'photoUrl': photoUrl,
        }),
      );

      print('Google login response status: ${response.statusCode}');
      print('Google login response body: ${response.body}');

      if (response.statusCode == 200) {
        return LoginResponse.fromJson(jsonDecode(response.body));
      } else {
        try {
          final error = jsonDecode(response.body);
          throw Exception(error['message'] ?? 'Google login failed with status ${response.statusCode}');
        } catch (e) {
          if (e is FormatException) {
            throw Exception('Server returned invalid response: ${response.statusCode}');
          }
          rethrow;
        }
      }
    } catch (e) {
      print('Google login error: $e');
      String message = e.toString().replaceAll('Exception: ', '');
      throw Exception(message);
    }
  }

  static Future<LoginResponse> register(
    String username,
    String email,
    String password, {
    String? fullName,
  }) async {
    try {
      print('Attempting registration for: $username');
      final response = await http.post(
        Uri.parse('$baseUrl/register'),
        headers: ApiConfig.headers,
        body: jsonEncode({
          'username': username,
          'email': email,
          'password': password,
          'fullName': fullName,
        }),
      );

      print('Register response status: ${response.statusCode}');
      print('Register response body: ${response.body}');

      if (response.statusCode == 201) {
        return LoginResponse.fromJson(jsonDecode(response.body));
      } else {
        try {
          final error = jsonDecode(response.body);
          throw Exception(error['message'] ?? 'Registration failed');
        } catch (e) {
          if (e is FormatException) {
            throw Exception('Server returned invalid response: ${response.statusCode}');
          }
          rethrow;
        }
      }
    } catch (e) {
      print('Registration error: $e');
      String message = e.toString().replaceAll('Exception: ', '');
      throw Exception(message);
    }
  }

  static Future<void> getCurrentUser(String token) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/me'),
        headers: {
          ...ApiConfig.headers,
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode != 200) {
        throw Exception('Failed to load user info');
      }
    } catch (e) {
      throw Exception('Failed to load user info: ${e.toString()}');
    }
  }

  static Future<void> logout(String token) async {
    try {
      await http.post(
        Uri.parse('$baseUrl/logout'),
        headers: {
          ...ApiConfig.headers,
          'Authorization': 'Bearer $token',
        },
      );
    } catch (e) {
      // Ignore logout errors
    }
  }
}
