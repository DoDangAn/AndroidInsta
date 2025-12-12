import 'dart:convert';

/// Backend error response models
/// Matches Spring Boot GlobalExceptionHandler error DTOs

class ErrorResponse {
  final bool success;
  final int status;
  final String error;
  final String message;
  final String? path;
  final String? timestamp;
  final Map<String, String>? details;

  ErrorResponse({
    this.success = false,
    required this.status,
    required this.error,
    required this.message,
    this.path,
    this.timestamp,
    this.details,
  });

  factory ErrorResponse.fromJson(Map<String, dynamic> json) {
    return ErrorResponse(
      success: json['success'] ?? false,
      status: json['status'] ?? 500,
      error: json['error'] ?? 'Unknown Error',
      message: json['message'] ?? 'An error occurred',
      path: json['path'],
      timestamp: json['timestamp'],
      details: json['details'] != null
          ? Map<String, String>.from(json['details'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'status': status,
      'error': error,
      'message': message,
      'path': path,
      'timestamp': timestamp,
      'details': details,
    };
  }

  @override
  String toString() => message;
}

class ValidationErrorResponse {
  final bool success;
  final int status;
  final String error;
  final String message;
  final Map<String, String> fieldErrors;
  final String? timestamp;

  ValidationErrorResponse({
    this.success = false,
    this.status = 400,
    this.error = 'Validation Error',
    required this.message,
    required this.fieldErrors,
    this.timestamp,
  });

  factory ValidationErrorResponse.fromJson(Map<String, dynamic> json) {
    return ValidationErrorResponse(
      success: json['success'] ?? false,
      status: json['status'] ?? 400,
      error: json['error'] ?? 'Validation Error',
      message: json['message'] ?? 'Input validation failed',
      fieldErrors: json['fieldErrors'] != null
          ? Map<String, String>.from(json['fieldErrors'])
          : {},
      timestamp: json['timestamp'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'status': status,
      'error': error,
      'message': message,
      'fieldErrors': fieldErrors,
      'timestamp': timestamp,
    };
  }

  String getFieldErrorsString() {
    return fieldErrors.entries
        .map((e) => '${e.key}: ${e.value}')
        .join('\n');
  }

  @override
  String toString() => '$message\n${getFieldErrorsString()}';
}

/// Helper to parse error from HTTP response
class ApiErrorParser {
  static Exception parseError(int statusCode, String responseBody) {
    try {
      // Try to decode JSON
      final decoded = _tryDecodeJson(responseBody);
      if (decoded == null) {
        return Exception('HTTP $statusCode: Invalid response');
      }

      // Check if validation error (has fieldErrors)
      if (decoded['fieldErrors'] != null) {
        final validationError = ValidationErrorResponse.fromJson(decoded);
        return Exception(validationError.toString());
      }

      // Check if standard error response
      if (decoded['error'] != null || decoded['message'] != null) {
        final error = ErrorResponse.fromJson(decoded);
        return Exception(error.message);
      }

      // Fallback
      return Exception(decoded['message'] ?? 'HTTP $statusCode: Error');
    } catch (e) {
      return Exception('HTTP $statusCode: ${responseBody.isEmpty ? "Unknown error" : responseBody}');
    }
  }

  static Map<String, dynamic>? _tryDecodeJson(String body) {
    try {
      return jsonDecode(body) as Map<String, dynamic>;
    } catch (e) {
      return null;
    }
  }
}
