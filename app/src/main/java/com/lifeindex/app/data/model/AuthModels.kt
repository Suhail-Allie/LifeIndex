package com.lifeindex.app.data.model

data class RegisterRequest(
    val displayName: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val id: String,
    val display_name: String,
    val email: String
)

data class AuthResponse(
    val message: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserResponse? = null
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class MessageResponse(
    val message: String
)