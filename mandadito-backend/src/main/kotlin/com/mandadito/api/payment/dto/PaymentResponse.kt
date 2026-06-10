package com.mandadito.api.payment.dto

import com.mandadito.api.order.PaymentMethod
import com.mandadito.api.order.PaymentStatus
import com.mandadito.api.payment.Payment
import java.time.LocalDateTime

data class PaymentResponse(
    val id: Long,
    val orderId: Long,
    val userId: Long,
    val amount: Double,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val reference: String?,
    val notes: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(p: Payment) = PaymentResponse(
            id = p.id,
            orderId = p.orderId,
            userId = p.userId,
            amount = p.amount,
            method = p.method,
            status = p.status,
            reference = p.reference,
            notes = p.notes,
            createdAt = p.createdAt
        )
    }
}
