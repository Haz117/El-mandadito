package com.mandadito.api.admin

import com.mandadito.api.business.dto.BusinessResponse
import com.mandadito.api.common.ApiResponse
import com.mandadito.api.order.dto.OrderResponse
import com.mandadito.api.restaurant.dto.RestaurantResponse
import com.mandadito.api.user.dto.UserProfileResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Panel de administración — solo ADMIN")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(private val adminService: AdminService) {

    // ─── Businesses ───────────────────────────────────────────────────────────

    @GetMapping("/businesses/pending")
    @Operation(summary = "Negocios pendientes de aprobación")
    fun getPendingBusinesses(): ResponseEntity<ApiResponse<List<BusinessResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(adminService.getPendingBusinesses()))

    @PatchMapping("/businesses/{id}/approve")
    @Operation(summary = "Aprobar negocio")
    fun approveBusiness(@PathVariable id: Long): ResponseEntity<ApiResponse<BusinessResponse>> =
        ResponseEntity.ok(ApiResponse.ok(adminService.approveBusiness(id), "Negocio aprobado"))

    @PatchMapping("/businesses/{id}/reject")
    @Operation(summary = "Rechazar negocio")
    fun rejectBusiness(@PathVariable id: Long): ResponseEntity<ApiResponse<BusinessResponse>> =
        ResponseEntity.ok(ApiResponse.ok(adminService.rejectBusiness(id), "Negocio rechazado"))

    @PatchMapping("/businesses/{id}/suspend")
    @Operation(summary = "Suspender negocio")
    fun suspendBusiness(@PathVariable id: Long): ResponseEntity<ApiResponse<BusinessResponse>> =
        ResponseEntity.ok(ApiResponse.ok(adminService.suspendBusiness(id), "Negocio suspendido"))

    // ─── Orders ───────────────────────────────────────────────────────────────

    @GetMapping("/orders")
    @Operation(summary = "Todos los pedidos")
    fun getAllOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<OrderResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(adminService.getAllOrders(page, size)))

    // ─── Users ────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "Todos los usuarios")
    fun getAllUsers(): ResponseEntity<ApiResponse<List<UserProfileResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()))

    // ─── Restaurants ──────────────────────────────────────────────────────────

    @GetMapping("/restaurants")
    @Operation(summary = "Todos los restaurantes (aprobados y no aprobados)")
    fun getAllRestaurants(): ResponseEntity<ApiResponse<List<RestaurantResponse>>> =
        ResponseEntity.ok(ApiResponse.ok(adminService.getAllRestaurants()))
}
