package com.mandadito.api.restaurant

import com.mandadito.api.business.BusinessRepository
import com.mandadito.api.restaurant.dto.CreateRestaurantRequest
import com.mandadito.api.restaurant.dto.RestaurantResponse
import com.mandadito.api.restaurant.dto.UpdateRestaurantRequest
import com.mandadito.api.user.UserService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
    private val businessRepository: BusinessRepository,
    private val userService: UserService
) {

    fun getAll(category: String?, search: String?): List<RestaurantResponse> {
        val restaurants = when {
            !search.isNullOrBlank() -> restaurantRepository.searchApproved(search)
            !category.isNullOrBlank() -> restaurantRepository.findApprovedByCategory(category)
            else -> restaurantRepository.findAllApproved()
        }
        return restaurants.map { RestaurantResponse.from(it) }
    }

    fun getById(id: Long): RestaurantResponse {
        val restaurant = restaurantRepository.findApprovedById(id)
            ?: throw NoSuchElementException("Restaurante $id no encontrado")
        return RestaurantResponse.from(restaurant)
    }

    fun getNearby(lat: Double, lng: Double, radiusKm: Double = 10.0): List<RestaurantResponse> =
        restaurantRepository.findNearby(lat, lng, radiusKm).map { RestaurantResponse.from(it) }

    fun getMyRestaurants(): List<RestaurantResponse> {
        val user = userService.getCurrentUser()
        val myBusinessIds = businessRepository.findByOwnerId(user.id).map { it.id }
        return myBusinessIds.flatMap { restaurantRepository.findByBusinessId(it) }
            .map { RestaurantResponse.from(it) }
    }

    @Transactional
    fun create(businessId: Long, request: CreateRestaurantRequest): RestaurantResponse {
        val user = userService.getCurrentUser()
        val business = businessRepository.findById(businessId)
            .orElseThrow { NoSuchElementException("Negocio $businessId no encontrado") }
        if (business.ownerId != user.id) {
            throw AccessDeniedException("No tienes permiso para crear restaurantes en este negocio")
        }
        val restaurant = Restaurant(
            businessId = businessId,
            name = request.name,
            description = request.description,
            category = request.category,
            imageUrl = request.imageUrl,
            coverImageUrl = request.coverImageUrl,
            deliveryTimeMin = request.deliveryTimeMin,
            deliveryTimeMax = request.deliveryTimeMax,
            deliveryFee = request.deliveryFee,
            latitude = request.latitude,
            longitude = request.longitude
        )
        return RestaurantResponse.from(restaurantRepository.save(restaurant))
    }

    @Transactional
    fun update(id: Long, request: UpdateRestaurantRequest): RestaurantResponse {
        val restaurant = validateOwnerAndGetRestaurant(id)
        restaurant.name = request.name
        restaurant.description = request.description
        restaurant.category = request.category
        restaurant.imageUrl = request.imageUrl
        restaurant.coverImageUrl = request.coverImageUrl
        restaurant.deliveryTimeMin = request.deliveryTimeMin
        restaurant.deliveryTimeMax = request.deliveryTimeMax
        restaurant.deliveryFee = request.deliveryFee
        restaurant.latitude = request.latitude
        restaurant.longitude = request.longitude
        restaurant.updatedAt = LocalDateTime.now()
        return RestaurantResponse.from(restaurantRepository.save(restaurant))
    }

    @Transactional
    fun toggleOpen(id: Long): RestaurantResponse {
        val restaurant = validateOwnerAndGetRestaurant(id)
        restaurant.isOpen = !restaurant.isOpen
        restaurant.updatedAt = LocalDateTime.now()
        return RestaurantResponse.from(restaurantRepository.save(restaurant))
    }

    private fun validateOwnerAndGetRestaurant(restaurantId: Long): Restaurant {
        val user = userService.getCurrentUser()
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { NoSuchElementException("Restaurante $restaurantId no encontrado") }
        val business = businessRepository.findById(restaurant.businessId)
            .orElseThrow { NoSuchElementException("Negocio no encontrado") }
        if (business.ownerId != user.id) {
            throw AccessDeniedException("No tienes permiso para modificar este restaurante")
        }
        return restaurant
    }
}
