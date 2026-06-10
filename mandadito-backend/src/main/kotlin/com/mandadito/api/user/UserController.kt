package com.mandadito.api.user

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.favorite.FavoriteRepository
import com.mandadito.api.order.OrderRepository
import com.mandadito.api.order.OrderStatus
import com.mandadito.api.user.dto.UpdateProfileRequest
import com.mandadito.api.user.dto.UserProfileResponse
import com.mandadito.api.user.dto.UserStatsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestión del perfil de usuario")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService,
    private val orderRepository: OrderRepository,
    private val favoriteRepository: FavoriteRepository
) {

    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario autenticado")
    fun getProfile(): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val user = userService.getCurrentUser()
        return ResponseEntity.ok(ApiResponse.ok(UserProfileResponse.from(user)))
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil")
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val user = userService.getCurrentUser()
        val updated = userService.updateProfile(user, request.name, request.phone)
        return ResponseEntity.ok(ApiResponse.ok(UserProfileResponse.from(updated), "Perfil actualizado"))
    }

    @GetMapping("/me/stats")
    @Operation(summary = "Estadísticas del usuario autenticado")
    fun getStats(): ResponseEntity<ApiResponse<UserStatsResponse>> {
        val user = userService.getCurrentUser()
        val orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.id)
        val stats = UserStatsResponse(
            totalOrders = orders.size.toLong(),
            deliveredOrders = orders.count { it.status == OrderStatus.DELIVERED }.toLong(),
            cancelledOrders = orders.count { it.status == OrderStatus.CANCELLED }.toLong(),
            totalSpent = orders.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.total },
            mandapoints = user.mandapoints,
            rating = user.rating,
            favoritesCount = favoriteRepository.findByUserId(user.id).size.toLong()
        )
        return ResponseEntity.ok(ApiResponse.ok(stats))
    }

    @GetMapping("/me/orders")
    @Operation(summary = "Historial de pedidos del usuario")
    fun getMyOrders(): ResponseEntity<ApiResponse<List<Map<String, Any>>>> {
        val user = userService.getCurrentUser()
        val orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.id)
        val summary = orders.map { o ->
            mapOf(
                "id" to o.id,
                "restaurantId" to o.restaurantId,
                "status" to o.status,
                "total" to o.total,
                "createdAt" to o.createdAt
            )
        }
        return ResponseEntity.ok(ApiResponse.ok(summary))
    }
}
