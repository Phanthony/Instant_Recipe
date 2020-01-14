package com.example.androidtraining.service.error

// When a HTTP response comes in at 401
class UnauthorizedException(message: String): Throwable(message)