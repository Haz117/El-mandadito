package com.mandadito.api.cart

import com.mandadito.api.cart.dto.AddCartItemRequest
import com.mandadito.api.cart.dto.CartResponse
import com.mandadito.api.menu.MenuItemRepository
import com.mandadito.api.restaurant.RestaurantRepository
import com.mandadito.api.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val menuItemRepository: MenuItemRepository,
    private val restaurantRepository: RestaurantRepository,
    private val userService: UserService
) {

    fun getCart(): CartResponse {
        val user = userService.getCurrentUser()
        val cart = cartRepository.findByUserId(user.id).orElseGet {
            cartRepository.save(Cart(userId = user.id))
        }
        return buildCartResponse(cart)
    }

    @Transactional
    fun addItem(request: AddCartItemRequest): CartResponse {
        val user = userService.getCurrentUser()
        val menuItem = menuItemRepository.findById(request.menuItemId)
            .orElseThrow { NoSuchElementException("Producto ${request.menuItemId} no encontrado") }

        if (!menuItem.available) {
            throw IllegalArgumentException("El producto '${menuItem.name}' no está disponible")
        }

        val restaurant = restaurantRepository.findById(menuItem.restaurantId)
            .orElseThrow { NoSuchElementException("Restaurante no encontrado") }

        val cart = cartRepository.findByUserId(user.id).orElseGet {
            cartRepository.save(Cart(userId = user.id))
        }

        if (cart.restaurantId != null && cart.restaurantId != restaurant.id) {
            throw IllegalArgumentException(
                "Tu carrito tiene productos de otro restaurante. Vacíalo primero."
            )
        }

        cart.restaurantId = restaurant.id

        val existingItem = cart.items.find { it.menuItemId == request.menuItemId }
        if (existingItem != null) {
            existingItem.quantity += request.quantity
        } else {
            cart.items.add(
                CartItem(cart = cart, menuItemId = request.menuItemId, quantity = request.quantity, notes = request.notes)
            )
        }

        cart.updatedAt = LocalDateTime.now()
        cartRepository.save(cart)
        return buildCartResponse(cart)
    }

    @Transactional
    fun updateItem(cartItemId: Long, quantity: Int): CartResponse {
        val user = userService.getCurrentUser()
        val cart = cartRepository.findByUserId(user.id)
            .orElseThrow { NoSuchElementException("Carrito no encontrado") }

        val item = cart.items.find { it.id == cartItemId }
            ?: throw NoSuchElementException("Item $cartItemId no encontrado en el carrito")

        if (quantity <= 0) {
            cart.items.remove(item)
        } else {
            item.quantity = quantity
        }

        if (cart.items.isEmpty()) {
            cart.restaurantId = null
        }

        cart.updatedAt = LocalDateTime.now()
        cartRepository.save(cart)
        return buildCartResponse(cart)
    }

    @Transactional
    fun removeItem(cartItemId: Long): CartResponse {
        val user = userService.getCurrentUser()
        val cart = cartRepository.findByUserId(user.id)
            .orElseThrow { NoSuchElementException("Carrito no encontrado") }

        cart.items.removeIf { it.id == cartItemId }

        if (cart.items.isEmpty()) {
            cart.restaurantId = null
        }

        cart.updatedAt = LocalDateTime.now()
        cartRepository.save(cart)
        return buildCartResponse(cart)
    }

    @Transactional
    fun clearMyCart() {
        val user = userService.getCurrentUser()
        clearCart(user.id)
    }

    @Transactional
    fun clearCart(userId: Long) {
        cartRepository.findByUserId(userId).ifPresent { cart ->
            cart.items.clear()
            cart.restaurantId = null
            cart.updatedAt = LocalDateTime.now()
            cartRepository.save(cart)
        }
    }

    private fun buildCartResponse(cart: Cart): CartResponse {
        val menuItemIds = cart.items.map { it.menuItemId }.toSet()
        val menuItems = menuItemRepository.findAllById(menuItemIds).associateBy { it.id }
        return CartResponse.from(cart, menuItems)
    }
}
