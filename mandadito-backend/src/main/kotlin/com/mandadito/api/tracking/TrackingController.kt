package com.mandadito.api.tracking

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.tracking.dto.TrackingResponse
import com.mandadito.api.tracking.dto.UpdateLocationRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Tracking", description = "Seguimiento de pedidos en tiempo real")
@SecurityRequirement(name = "bearerAuth")
class TrackingController(private val trackingService: TrackingService) {

    @GetMapping("/{id}/tracking")
    @Operation(summary = "Estado de tracking del pedido")
    fun getTracking(@PathVariable id: Long): ResponseEntity<ApiResponse<TrackingResponse>> =
        ResponseEntity.ok(ApiResponse.ok(trackingService.getTracking(id)))

    @GetMapping("/{id}/events")
    @Operation(summary = "Historial de eventos del pedido")
    fun getEvents(@PathVariable id: Long): ResponseEntity<ApiResponse<List<TrackingResponse.EventDto>>> =
        ResponseEntity.ok(ApiResponse.ok(trackingService.getEvents(id)))

    @PatchMapping("/{id}/location")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Actualizar ubicación del repartidor")
    fun updateLocation(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateLocationRequest
    ): ResponseEntity<ApiResponse<Nothing?>> {
        trackingService.updateLocation(id, request)
        return ResponseEntity.ok(ApiResponse.ok("Ubicación actualizada"))
    }
}
