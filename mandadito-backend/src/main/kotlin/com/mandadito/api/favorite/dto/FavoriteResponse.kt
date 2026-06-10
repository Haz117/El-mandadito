package com.mandadito.api.favorite.dto

import com.mandadito.api.favorite.Favorite
import com.mandadito.api.restaurant.dto.RestaurantResponse
import java.time.LocalDateTime

data class FavoriteResponse(
    val id: Long,
    val restaurantId: Long,
    val restaurant: RestaurantResponse?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(favorite: Favorite, restaurant: RestaurantResponse? = null) = FavoriteResponse(
            id = favorite.id,
            restaurantId = favorite.restaurantId,
            restaurant = restaurant,
            createdAt = favorite.createdAt
        )
    }
}
