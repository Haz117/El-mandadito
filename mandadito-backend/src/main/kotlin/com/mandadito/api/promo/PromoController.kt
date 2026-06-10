package com.mandadito.api.promo

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.promo.dto.CreatePromotionRequest
import com.mandadito.api.promo.dto.PromotionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/promotions")
@Tag(name = "Promotions", description = "Promociones y banners")
@SecurityRequirement(name = "bearerAuth")
class PromoController(private val promoService: PromoService) {

    @GetMapping
    @Operation(summary = "Promociones activas vigentes")
    fun getActivePromotions(): ResponseEntity<ApiResponse<List<PromotionResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(promoService.getActivePromotions()))

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una promoción")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<PromotionResponse>> =
        ResponseEntity.ok(ApiResponse.ok(promoService.getById(id)))

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear promoción")
    fun create(
        @Valid @RequestBody request: CreatePromotionRequest
    ): ResponseEntity<ApiResponse<PromotionResponse>> {
        val response = promoService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Promoción creada"))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar promoción")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreatePromotionRequest
    ): ResponseEntity<ApiResponse<PromotionResponse>> =
        ResponseEntity.ok(ApiResponse.ok(promoService.update(id, request), "Promoción actualizada"))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar promoción")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing?>> {
        promoService.delete(id)
        return ResponseEntity.ok(ApiResponse.ok("Promoción eliminada"))
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar promoción")
    fun toggleStatus(@PathVariable id: Long): ResponseEntity<ApiResponse<PromotionResponse>> =
        ResponseEntity.ok(ApiResponse.ok(promoService.toggleActive(id)))
}
