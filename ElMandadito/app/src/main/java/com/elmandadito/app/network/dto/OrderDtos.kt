package com.elmandadito.app.network.dto

import java.time.LocalDateTime

data class CreateOrderRequest(
    val addressId: Long,
    val paymentMethod: String = "CASH",
    val notes: String? = null
)

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val restaurantId: Long,
    val status: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val notes: String?,
    val items: List<OrderItemResponse>,
    val createdAt: String
)

data class OrderItemResponse(
    val id: Long,
    val menuItemId: Long?,
    val name: String,
    val price: Double,
    val quantity: Int,
    val subtotal: Double
)

data class AddCartItemRequest(
    val menuItemId: Long,
    val quantity: Int = 1,
    val notes: String? = null
)

data class CartResponse(
    val restaurantId: Long?,
    val items: List<CartItemDetail>,
    val total: Double
) {
    data class CartItemDetail(
        val id: Long,
        val menuItemId: Long,
        val name: String,
        val price: Double,
        val quantity: Int,
        val subtotal: Double
    )
}
