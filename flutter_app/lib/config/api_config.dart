/// API Configuration for AndroidInsta
class ApiConfig {
  // Backend base URL
  // For Android Emulator: Use 10.0.2.2
  // For iOS Simulator: Use localhost or 127.0.0.1
  // For Physical Device: Use your computer's IP address (e.g., 192.168.1.x)
  static const String baseUrl = 'http://10.0.2.2:8081';
  
  // API endpoints
  static const String authUrl = '$baseUrl/api/auth';
  static const String usersUrl = '$baseUrl/api/users';
  static const String postsUrl = '$baseUrl/api/posts';
  static const String chatUrl = '$baseUrl/api/chat';
  static const String webSocketUrl = 'ws://10.0.2.2:8081/ws';
  
  // Timeout durations
  static const Duration connectTimeout = Duration(seconds: 30);
  static const Duration receiveTimeout = Duration(seconds: 30);
  
  // Headers
  static Map<String, String> get headers => {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
  
  static Map<String, String> getAuthHeaders(String token) => {
    ...headers,
    'Authorization': 'Bearer $token',
  };
}
