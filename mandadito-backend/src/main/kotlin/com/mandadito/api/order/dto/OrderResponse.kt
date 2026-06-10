package com.mandadito.api.order.dto

import com.mandadito.api.order.Order
import com.mandadito.api.order.OrderStatus
import com.mandadito.api.order.PaymentMethod
import com.mandadito.api.order.PaymentStatus
import java.time.LocalDateTime

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val restaurantId: Long,
    val driverId: Long?,
    val addressId: Long,
    val status: OrderStatus,
    val subtotal: Double,
    val deliveryFee: Double,
    val serviceFee: Double,
    val discount: Double,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val notes: String?,
    val items: List<OrderItemResponse>,
    val createdAt: LocalDateTime,
    val deliveredAt: LocalDateTime?,
    val cancelledAt: LocalDateTime?
) {
    companion object {
        fun from(order: Order) = OrderResponse(
            id = order.id,
            userId = order.userId,
            restaurantId = order.restaurantId,
            driverId = order.driverId,
            addressId = order.addressId,
            status = order.status,
            subtotal = order.subtotal,
            deliveryFee = order.deliveryFee,
            serviceFee = order.serviceFee,
            discount = order.discount,
            total = order.total,
            paymentMethod = order.paymentMethod,
            paymentStatus = order.paymentStatus,
            notes = order.notes,
            items = order.items.map { OrderItemResponse.from(it) },
            createdAt = order.createdAt,
            deliveredAt = order.deliveredAt,
            cancelledAt = order.cancelledAt
        )
    }
}
