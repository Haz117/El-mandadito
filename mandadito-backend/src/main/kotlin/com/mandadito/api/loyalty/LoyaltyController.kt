package com.mandadito.api.loyalty

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.loyalty.dto.LoyaltyResponse
import com.mandadito.api.loyalty.dto.RedeemRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/loyalty")
@Tag(name = "Loyalty", description = "Sistema de puntos Mandapoints")
@SecurityRequirement(name = "bearerAuth")
class LoyaltyController(private val loyaltyService: LoyaltyService) {

    @GetMapping("/me")
    @Operation(summary = "Mis Mandapoints y nivel")
    fun getMyLoyalty(): ResponseEntity<ApiResponse<LoyaltyResponse>> =
        ResponseEntity.ok(ApiResponse.ok(loyaltyService.getMyLoyalty()))

    @GetMapping("/history")
    @Operation(summary = "Historial de puntos")
    fun getHistory(): ResponseEntity<ApiResponse<LoyaltyResponse>> =
        ResponseEntity.ok(ApiResponse.ok(loyaltyService.getMyLoyalty()))

    @PostMapping("/redeem")
    @Operation(summary = "Canjear Mandapoints")
    fun redeem(
        @Valid @RequestBody request: RedeemRequest
    ): ResponseEntity<ApiResponse<Nothing?>> {
        loyaltyService.redeemPoints(request)
        return ResponseEntity.ok(ApiResponse.ok("${request.points} Mandapoints canjeados"))
    }
}
