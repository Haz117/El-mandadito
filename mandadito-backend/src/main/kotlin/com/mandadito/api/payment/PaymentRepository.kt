package com.mandadito.api.payment

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: Long): Optional<Payment>
    fun findByUserId(userId: Long): List<Payment>
}
