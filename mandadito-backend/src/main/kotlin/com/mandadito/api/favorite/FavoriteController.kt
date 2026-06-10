package com.mandadito.api.favorite

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.favorite.dto.FavoriteResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "Restaurantes favoritos")
@SecurityRequirement(name = "bearerAuth")
class FavoriteController(private val favoriteService: FavoriteService) {

    @GetMapping
    @Operation(summary = "Mis restaurantes favoritos")
    fun getMyFavorites(): ResponseEntity<ApiResponse<List<FavoriteResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(favoriteService.getMyFavorites()))

    @PostMapping("/{restaurantId}")
    @Operation(summary = "Agregar restaurante a favoritos")
    fun addFavorite(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<FavoriteResponse>> {
        val response = favoriteService.addFavorite(restaurantId)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Agregado a favoritos"))
    }

    @DeleteMapping("/{restaurantId}")
    @Operation(summary = "Quitar restaurante de favoritos")
    fun removeFavorite(@PathVariable restaurantId: Long): ResponseEntity<ApiResponse<Nothing?>> {
        favoriteService.removeFavorite(restaurantId)
        return ResponseEntity.ok(ApiResponse.ok("Eliminado de favoritos"))
    }
}
