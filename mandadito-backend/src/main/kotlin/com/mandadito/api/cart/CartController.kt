package com.mandadito.api.cart

import com.mandadito.api.cart.dto.AddCartItemRequest
import com.mandadito.api.cart.dto.CartResponse
import com.mandadito.api.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Carrito de compras")
@SecurityRequirement(name = "bearerAuth")
class CartController(private val cartService: CartService) {

    @GetMapping
    @Operation(summary = "Ver carrito actual")
    fun getCart(): ResponseEntity<ApiResponse<CartResponse>> =
        ResponseEntity.ok(ApiResponse.ok(cartService.getCart()))

    @PostMapping("/items")
    @Operation(summary = "Agregar producto al carrito")
    fun addItem(
        @Valid @RequestBody request: AddCartItemRequest
    ): ResponseEntity<ApiResponse<CartResponse>> =
        ResponseEntity.ok(ApiResponse.ok(cartService.addItem(request), "Producto agregado"))

    @PutMapping("/items/{id}")
    @Operation(summary = "Cambiar cantidad de un item (0 = eliminar)")
    fun updateItem(
        @PathVariable id: Long,
        @RequestParam quantity: Int
    ): ResponseEntity<ApiResponse<CartResponse>> =
        ResponseEntity.ok(ApiResponse.ok(cartService.updateItem(id, quantity)))

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Eliminar item del carrito")
    fun removeItem(@PathVariable id: Long): ResponseEntity<ApiResponse<CartResponse>> =
        ResponseEntity.ok(ApiResponse.ok(cartService.removeItem(id)))

    @DeleteMapping
    @Operation(summary = "Vaciar carrito")
    fun clearCart(): ResponseEntity<ApiResponse<Nothing?>> {
        cartService.clearMyCart()
        return ResponseEntity.ok(ApiResponse.ok("Carrito vaciado"))
    }
}
