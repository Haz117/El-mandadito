package com.mandadito.api.menu

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.menu.dto.CreateMenuItemRequest
import com.mandadito.api.menu.dto.MenuItemResponse
import com.mandadito.api.menu.dto.UpdateMenuItemRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Menu", description = "Menú de restaurantes")
@SecurityRequirement(name = "bearerAuth")
class MenuController(private val menuService: MenuService) {

    @GetMapping("/api/restaurants/{restaurantId}/menu")
    @Operation(summary = "Ver menú disponible de un restaurante")
    fun getMenu(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<List<MenuItemResponse>>> {
        val response = menuService.getMenu(restaurantId)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PostMapping("/api/restaurants/{restaurantId}/menu")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Agregar producto al menú")
    fun create(
        @PathVariable restaurantId: Long,
        @Valid @RequestBody request: CreateMenuItemRequest
    ): ResponseEntity<ApiResponse<MenuItemResponse>> {
        val response = menuService.create(restaurantId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Producto agregado"))
    }

    @PutMapping("/api/menu-items/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Actualizar producto del menú")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMenuItemRequest
    ): ResponseEntity<ApiResponse<MenuItemResponse>> {
        val response = menuService.update(id, request)
        return ResponseEntity.ok(ApiResponse.ok(response, "Producto actualizado"))
    }

    @DeleteMapping("/api/menu-items/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Eliminar producto del menú")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing?>> {
        menuService.delete(id)
        return ResponseEntity.ok(ApiResponse.ok("Producto eliminado"))
    }

    @PatchMapping("/api/menu-items/{id}/availability")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Activar o desactivar disponibilidad del producto")
    fun toggleAvailability(@PathVariable id: Long): ResponseEntity<ApiResponse<MenuItemResponse>> {
        val response = menuService.toggleAvailability(id)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
