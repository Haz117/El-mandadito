package com.mandadito.api.order.dto

import com.mandadito.api.order.PaymentMethod
import jakarta.validation.constraints.NotNull

data class CreateOrderRequest(
    @field:NotNull(message = "La dirección de entrega es obligatoria")
    val addressId: Long,

    val paymentMethod: PaymentMethod = PaymentMethod.CASH,

    val notes: String? = null
)
