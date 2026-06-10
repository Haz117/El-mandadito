package com.mandadito.api.auth.dto

import com.mandadito.api.user.Role
import com.mandadito.api.user.User

data class AuthResponse(
    val token: String,
    val user: AuthUserDto
)

data class AuthUserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: Role,
    val profileImageUrl: String?,
    val mandapoints: Int
) {
    companion object {
        fun from(user: User) = AuthUserDto(
            id = user.id,
            name = user.name,
            email = user.email,
            role = user.role,
            profileImageUrl = user.profileImageUrl,
            mandapoints = user.mandapoints
        )
    }
}
