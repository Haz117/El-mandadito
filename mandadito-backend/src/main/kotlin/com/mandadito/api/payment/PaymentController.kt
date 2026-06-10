package com.mandadito.api.payment

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.payment.dto.CreatePaymentRequest
import com.mandadito.api.payment.dto.PaymentResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Gestión de pagos")
@SecurityRequirement(name = "bearerAuth")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping("/create")
    @Operation(summary = "Crear pago para un pedido (CASH/TRANSFER)")
    fun createPayment(
        @Valid @RequestBody request: CreatePaymentRequest
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        val response = paymentService.createPayment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Pago creado"))
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_OWNER')")
    @Operation(summary = "Confirmar pago recibido")
    fun confirmPayment(@PathVariable id: Long): ResponseEntity<ApiResponse<PaymentResponse>> =
        ResponseEntity.ok(ApiResponse.ok(paymentService.confirmPayment(id), "Pago confirmado"))

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un pago")
    fun getPayment(@PathVariable id: Long): ResponseEntity<ApiResponse<PaymentResponse>> =
        ResponseEntity.ok(ApiResponse.ok(paymentService.getPayment(id)))

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reembolsar pago")
    fun refundPayment(@PathVariable id: Long): ResponseEntity<ApiResponse<PaymentResponse>> =
        ResponseEntity.ok(ApiResponse.ok(paymentService.refundPayment(id), "Reembolso procesado"))
}
