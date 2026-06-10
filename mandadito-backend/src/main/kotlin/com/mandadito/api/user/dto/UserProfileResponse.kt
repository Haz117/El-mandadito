package com.mandadito.api.user.dto

import com.mandadito.api.user.Role
import com.mandadito.api.user.User
import java.time.LocalDateTime

data class UserProfileResponse(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val role: Role,
    val profileImageUrl: String?,
    val rating: Double,
    val mandapoints: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: User) = UserProfileResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone,
            role = user.role,
            profileImageUrl = user.profileImageUrl,
            rating = user.rating,
            mandapoints = user.mandapoints,
            createdAt = user.createdAt
        )
    }
}
