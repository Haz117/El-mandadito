package com.mandadito.api.menu

import com.mandadito.api.business.BusinessRepository
import com.mandadito.api.menu.dto.CreateMenuItemRequest
import com.mandadito.api.menu.dto.MenuItemResponse
import com.mandadito.api.menu.dto.UpdateMenuItemRequest
import com.mandadito.api.restaurant.RestaurantRepository
import com.mandadito.api.user.UserService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MenuService(
    private val menuItemRepository: MenuItemRepository,
    private val restaurantRepository: RestaurantRepository,
    private val businessRepository: BusinessRepository,
    private val userService: UserService
) {

    fun getMenu(restaurantId: Long): List<MenuItemResponse> {
        restaurantRepository.findApprovedById(restaurantId)
            ?: throw NoSuchElementException("Restaurante $restaurantId no encontrado")
        return menuItemRepository.findByRestaurantIdAndAvailable(restaurantId, true)
            .map { MenuItemResponse.from(it) }
    }

    fun getFullMenu(restaurantId: Long): List<MenuItemResponse> =
        menuItemRepository.findByRestaurantId(restaurantId).map { MenuItemResponse.from(it) }

    @Transactional
    fun create(restaurantId: Long, request: CreateMenuItemRequest): MenuItemResponse {
        validateRestaurantOwner(restaurantId)
        val item = MenuItem(
            restaurantId = restaurantId,
            name = request.name,
            description = request.description,
            price = request.price,
            imageUrl = request.imageUrl,
            category = request.category,
            available = request.available
        )
        return MenuItemResponse.from(menuItemRepository.save(item))
    }

    @Transactional
    fun update(itemId: Long, request: UpdateMenuItemRequest): MenuItemResponse {
        val item = menuItemRepository.findById(itemId)
            .orElseThrow { NoSuchElementException("Producto $itemId no encontrado") }
        validateRestaurantOwner(item.restaurantId)
        item.name = request.name
        item.description = request.description
        item.price = request.price
        item.imageUrl = request.imageUrl
        item.category = request.category
        item.available = request.available
        item.updatedAt = LocalDateTime.now()
        return MenuItemResponse.from(menuItemRepository.save(item))
    }

    @Transactional
    fun delete(itemId: Long) {
        val item = menuItemRepository.findById(itemId)
            .orElseThrow { NoSuchElementException("Producto $itemId no encontrado") }
        validateRestaurantOwner(item.restaurantId)
        menuItemRepository.delete(item)
    }

    @Transactional
    fun toggleAvailability(itemId: Long): MenuItemResponse {
        val item = menuItemRepository.findById(itemId)
            .orElseThrow { NoSuchElementException("Producto $itemId no encontrado") }
        validateRestaurantOwner(item.restaurantId)
        item.available = !item.available
        item.updatedAt = LocalDateTime.now()
        return MenuItemResponse.from(menuItemRepository.save(item))
    }

    private fun validateRestaurantOwner(restaurantId: Long) {
        val user = userService.getCurrentUser()
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { NoSuchElementException("Restaurante $restaurantId no encontrado") }
        val business = businessRepository.findById(restaurant.businessId)
            .orElseThrow { NoSuchElementException("Negocio no encontrado") }
        if (business.ownerId != user.id) {
            throw AccessDeniedException("No tienes permiso para modificar el menú de este restaurante")
        }
    }
}
