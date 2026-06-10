package com.mandadito.api.order.dto

import com.mandadito.api.order.OrderStatus
import jakarta.validation.constraints.NotNull

data class OrderStatusUpdateRequest(
    @field:NotNull(message = "El estado es obligatorio")
    val status: OrderStatus
)
