package com.mandadito.api.review

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.review.dto.CreateReviewRequest
import com.mandadito.api.review.dto.ReviewResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Reviews", description = "Calificaciones de pedidos")
@SecurityRequirement(name = "bearerAuth")
class ReviewController(private val reviewService: ReviewService) {

    @PostMapping("/api/reviews")
    @Operation(summary = "Calificar un pedido entregado")
    fun create(
        @Valid @RequestBody request: CreateReviewRequest
    ): ResponseEntity<ApiResponse<ReviewResponse>> {
        val response = reviewService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Calificación enviada"))
    }

    @GetMapping("/api/restaurants/{restaurantId}/reviews")
    @Operation(summary = "Reseñas de un restaurante")
    fun getRestaurantReviews(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<List<ReviewResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(reviewService.getRestaurantReviews(restaurantId)))

    @GetMapping("/api/drivers/{driverId}/reviews")
    @Operation(summary = "Reseñas de un repartidor")
    fun getDriverReviews(
        @PathVariable driverId: Long
    ): ResponseEntity<ApiResponse<List<ReviewResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(reviewService.getDriverReviews(driverId)))
}
