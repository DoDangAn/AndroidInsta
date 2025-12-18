import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/api_config.dart';
import '../models/user_models.dart';
import '../models/error_models.dart';

/// Backend returns AuthData directly (not wrapped)
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
      user: json['user'] != null ? UserProfile.fromJson(json['user']) : UserProfile.empty(),
    );
  }
}

class LoginService {
  static const String baseUrl = ApiConfig.authUrl;

  static Future<AuthData> login(String username, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/login'),
      headers: ApiConfig.headers,
      body: jsonEncode({
        'usernameOrEmail': username,
        'password': password,
      }),
    );

    if (response.statusCode == 200) {
      print('Login response: ${response.body}'); // Log response body
      final jsonResponse = jsonDecode(response.body);
      // Backend wraps response in {success, message, data}
      final data = jsonResponse['data'] ?? jsonResponse;
      return AuthData.fromJson(data);
    } else {
      print('Login error: ${response.statusCode}, ${response.body}'); // Log error details
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }
  
  static Future<AuthData> googleLogin(String email, String googleId, {String? fullName, String? photoUrl}) async {
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

    if (response.statusCode == 200) {
      final jsonResponse = jsonDecode(response.body);
      final data = jsonResponse['data'] ?? jsonResponse;
      return AuthData.fromJson(data);
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  static Future<AuthData> register(
    String username,
    String email,
    String password, {
    String? fullName,
  }) async {
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

    if (response.statusCode == 201) {
      final jsonResponse = jsonDecode(response.body);
      final data = jsonResponse['data'] ?? jsonResponse;
      return AuthData.fromJson(data);
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  static Future<UserProfile> getCurrentUser(String token) async {
    final response = await http.get(
      Uri.parse('$baseUrl/me'),
      headers: {
        ...ApiConfig.headers,
        'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      return UserProfile.fromJson(jsonDecode(response.body));
    } else {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }

  static Future<void> logout(String token) async {
    final response = await http.post(
      Uri.parse('$baseUrl/logout'),
      headers: {
        ...ApiConfig.headers,
        'Authorization': 'Bearer $token',
      },
    );
    
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw ApiErrorParser.parseError(response.statusCode, response.body);
    }
  }
}
