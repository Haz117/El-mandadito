package com.mandadito.api.restaurant

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.restaurant.dto.CreateRestaurantRequest
import com.mandadito.api.restaurant.dto.RestaurantResponse
import com.mandadito.api.restaurant.dto.UpdateRestaurantRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Listado y gestión de restaurantes")
@SecurityRequirement(name = "bearerAuth")
class RestaurantController(private val restaurantService: RestaurantService) {

    @GetMapping
    @Operation(summary = "Listar restaurantes aprobados (filtrar por categoría o buscar)")
    fun getAll(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<List<RestaurantResponse>>> {
        val response = restaurantService.getAll(category, search)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un restaurante")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<RestaurantResponse>> {
        val response = restaurantService.getById(id)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @GetMapping("/nearby")
    @Operation(summary = "Restaurantes cercanos por coordenadas")
    fun getNearby(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(defaultValue = "10.0") radiusKm: Double
    ): ResponseEntity<ApiResponse<List<RestaurantResponse>>> {
        val response = restaurantService.getNearby(lat, lng, radiusKm)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Mis restaurantes como dueño de negocio")
    fun getMyRestaurants(): ResponseEntity<ApiResponse<List<RestaurantResponse>>> {
        val response = restaurantService.getMyRestaurants()
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PostMapping("/businesses/{businessId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Crear restaurante bajo un negocio propio")
    fun create(
        @PathVariable businessId: Long,
        @Valid @RequestBody request: CreateRestaurantRequest
    ): ResponseEntity<ApiResponse<RestaurantResponse>> {
        val response = restaurantService.create(businessId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Restaurante creado"))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Actualizar restaurante")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRestaurantRequest
    ): ResponseEntity<ApiResponse<RestaurantResponse>> {
        val response = restaurantService.update(id, request)
        return ResponseEntity.ok(ApiResponse.ok(response, "Restaurante actualizado"))
    }

    @PatchMapping("/{id}/toggle-open")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Abrir o cerrar restaurante")
    fun toggleOpen(@PathVariable id: Long): ResponseEntity<ApiResponse<RestaurantResponse>> {
        val response = restaurantService.toggleOpen(id)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
