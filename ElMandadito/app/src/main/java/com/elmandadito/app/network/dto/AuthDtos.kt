package com.elmandadito.app.network.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val role: String = "USER"
)

data class AuthResponse(
    val token: String,
    val user: AuthUserDto
)

data class AuthUserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val profileImageUrl: String?,
    val mandapoints: Int
)
