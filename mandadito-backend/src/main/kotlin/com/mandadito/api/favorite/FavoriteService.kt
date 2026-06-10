package com.mandadito.api.favorite

import com.mandadito.api.favorite.dto.FavoriteResponse
import com.mandadito.api.restaurant.RestaurantRepository
import com.mandadito.api.restaurant.dto.RestaurantResponse
import com.mandadito.api.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val restaurantRepository: RestaurantRepository,
    private val userService: UserService
) {

    fun getMyFavorites(): List<FavoriteResponse> {
        val user = userService.getCurrentUser()
        val favorites = favoriteRepository.findByUserId(user.id)
        val restaurantIds = favorites.map { it.restaurantId }.toSet()
        val restaurants = restaurantRepository.findAllById(restaurantIds).associateBy { it.id }
        return favorites.map { fav ->
            FavoriteResponse.from(fav, restaurants[fav.restaurantId]?.let { RestaurantResponse.from(it) })
        }
    }

    @Transactional
    fun addFavorite(restaurantId: Long): FavoriteResponse {
        val user = userService.getCurrentUser()

        val restaurant = restaurantRepository.findApprovedById(restaurantId)
            ?: throw NoSuchElementException("Restaurante $restaurantId no encontrado o no disponible")

        if (favoriteRepository.existsByUserIdAndRestaurantId(user.id, restaurantId)) {
            throw IllegalArgumentException("El restaurante ya está en tus favoritos")
        }

        val favorite = favoriteRepository.save(Favorite(userId = user.id, restaurantId = restaurantId))
        return FavoriteResponse.from(favorite, RestaurantResponse.from(restaurant))
    }

    @Transactional
    fun removeFavorite(restaurantId: Long) {
        val user = userService.getCurrentUser()
        if (!favoriteRepository.existsByUserIdAndRestaurantId(user.id, restaurantId)) {
            throw NoSuchElementException("Restaurante no está en tus favoritos")
        }
        favoriteRepository.deleteByUserIdAndRestaurantId(user.id, restaurantId)
    }
}
