package com.mandadito.api.payment.dto

import com.mandadito.api.order.PaymentMethod
import jakarta.validation.constraints.NotNull

data class CreatePaymentRequest(
    @field:NotNull(message = "El ID del pedido es obligatorio")
    val orderId: Long,

    val method: PaymentMethod = PaymentMethod.CASH,

    val reference: String? = null
)
