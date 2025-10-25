package com.androidinsta.config

// JWT Exception Handler has been consolidated into GlobalExceptionHandler
// This file is kept for backwards compatibility and will be removed in future versions

class JwtException(message: String) : RuntimeException(message)