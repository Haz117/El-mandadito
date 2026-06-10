package com.mandadito.api.payment

import com.mandadito.api.order.OrderRepository
import com.mandadito.api.order.PaymentStatus
import com.mandadito.api.payment.dto.CreatePaymentRequest
import com.mandadito.api.payment.dto.PaymentResponse
import com.mandadito.api.user.UserService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val userService: UserService
) {

    @Transactional
    fun createPayment(request: CreatePaymentRequest): PaymentResponse {
        val user = userService.getCurrentUser()

        val order = orderRepository.findById(request.orderId)
            .orElseThrow { NoSuchElementException("Pedido ${request.orderId} no encontrado") }

        if (order.userId != user.id) throw AccessDeniedException("No puedes pagar un pedido que no es tuyo")

        if (paymentRepository.findByOrderId(request.orderId).isPresent) {
            throw IllegalArgumentException("Ya existe un pago para este pedido")
        }

        val payment = Payment(
            orderId = order.id,
            userId = user.id,
            amount = order.total,
            method = request.method,
            reference = request.reference
        )
        return PaymentResponse.from(paymentRepository.save(payment))
    }

    @Transactional
    fun confirmPayment(paymentId: Long): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { NoSuchElementException("Pago $paymentId no encontrado") }

        payment.status = PaymentStatus.PAID
        payment.updatedAt = LocalDateTime.now()

        val order = orderRepository.findById(payment.orderId).orElse(null)
        order?.paymentStatus = PaymentStatus.PAID
        order?.let { orderRepository.save(it) }

        return PaymentResponse.from(paymentRepository.save(payment))
    }

    fun getPayment(paymentId: Long): PaymentResponse {
        val user = userService.getCurrentUser()
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { NoSuchElementException("Pago $paymentId no encontrado") }
        if (payment.userId != user.id) throw AccessDeniedException("No tienes acceso a este pago")
        return PaymentResponse.from(payment)
    }

    @Transactional
    fun refundPayment(paymentId: Long): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { NoSuchElementException("Pago $paymentId no encontrado") }
        if (payment.status != PaymentStatus.PAID) {
            throw IllegalArgumentException("Solo se pueden reembolsar pagos completados")
        }
        payment.status = PaymentStatus.REFUNDED
        payment.updatedAt = LocalDateTime.now()
        return PaymentResponse.from(paymentRepository.save(payment))
    }
}
