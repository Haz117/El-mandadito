package com.mandadito.api.menu.dto

import com.mandadito.api.menu.MenuItem
import java.time.LocalDateTime

data class MenuItemResponse(
    val id: Long,
    val restaurantId: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val imageUrl: String?,
    val category: String?,
    val available: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(m: MenuItem) = MenuItemResponse(
            id = m.id,
            restaurantId = m.restaurantId,
            name = m.name,
            description = m.description,
            price = m.price,
            imageUrl = m.imageUrl,
            category = m.category,
            available = m.available,
            createdAt = m.createdAt
        )
    }
}
