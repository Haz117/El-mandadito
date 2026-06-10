package com.mandadito.api.order

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.order.dto.CreateOrderRequest
import com.mandadito.api.order.dto.OrderResponse
import com.mandadito.api.order.dto.OrderStatusUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Gestión de pedidos")
@SecurityRequirement(name = "bearerAuth")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    @Operation(summary = "Crear pedido desde el carrito")
    fun createOrder(
        @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val response = orderService.createOrder(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Pedido creado"))
    }

    @GetMapping
    @Operation(summary = "Mis pedidos")
    fun getMyOrders(): ResponseEntity<ApiResponse<List<OrderResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders()))

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un pedido")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<ApiResponse<OrderResponse>> =
        ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)))

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado del pedido (restaurante/repartidor)")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: OrderStatusUpdateRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> =
        ResponseEntity.ok(ApiResponse.ok(orderService.updateStatus(id, request.status)))

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido (usuario)")
    fun cancelOrder(@PathVariable id: Long): ResponseEntity<ApiResponse<OrderResponse>> =
        ResponseEntity.ok(ApiResponse.ok(orderService.cancelOrder(id), "Pedido cancelado"))

    @PatchMapping("/{id}/assign-driver")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_OWNER')")
    @Operation(summary = "Asignar repartidor al pedido")
    fun assignDriver(
        @PathVariable id: Long,
        @RequestParam driverId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> =
        ResponseEntity.ok(ApiResponse.ok(orderService.assignDriver(id, driverId)))
}
