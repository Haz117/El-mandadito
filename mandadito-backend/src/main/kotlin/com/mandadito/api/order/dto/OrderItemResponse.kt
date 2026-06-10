package com.mandadito.api.order.dto

import com.mandadito.api.order.OrderItem

data class OrderItemResponse(
    val id: Long,
    val menuItemId: Long?,
    val name: String,
    val price: Double,
    val quantity: Int,
    val subtotal: Double,
    val notes: String?
) {
    companion object {
        fun from(item: OrderItem) = OrderItemResponse(
            id = item.id,
            menuItemId = item.menuItemId,
            name = item.nameSnapshot,
            price = item.priceSnapshot,
            quantity = item.quantity,
            subtotal = item.subtotal,
            notes = item.notes
        )
    }
}
