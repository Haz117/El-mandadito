package com.elmandadito.app.network.dto

data class SupabaseCreateOrderRequest(
    val userId: String,                              // UUID del usuario autenticado
    val restaurantId: Long,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double = 0.0,
    val total: Double,
    val paymentMethod: String = "CASH",
    val paymentStatus: String = "PENDING",
    val status: String = "PENDING",
    val notes: String? = null,
    val address: String? = null
)

data class OrderResponse(
    val id: Long,
    val userId: String,
    val restaurantId: Long,
    val status: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double = 0.0,
    val total: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val notes: String?,
    val address: String?,
    val createdAt: String,
    val orderItems: List<OrderItemResponse> = emptyList()
)

data class OrderItemResponse(
    val id: Long,
    val orderId: Long,
    val menuItemId: Long?,
    val name: String,
    val price: Double,
    val quantity: Int,
    val subtotal: Double
)

// Mantenidos por compatibilidad con CartFragment y CartRepository existentes
data class CreateOrderRequest(
    val addressId: Long,
    val paymentMethod: String = "CASH",
    val notes: String? = null
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
