package com.mandadito.api.business.dto

import com.mandadito.api.business.Business
import com.mandadito.api.business.BusinessStatus
import java.time.LocalDateTime

data class BusinessResponse(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val description: String?,
    val logoUrl: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val city: String?,
    val status: BusinessStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(b: Business) = BusinessResponse(
            id = b.id,
            ownerId = b.ownerId,
            name = b.name,
            description = b.description,
            logoUrl = b.logoUrl,
            phone = b.phone,
            email = b.email,
            address = b.address,
            city = b.city,
            status = b.status,
            createdAt = b.createdAt
        )
    }
}
