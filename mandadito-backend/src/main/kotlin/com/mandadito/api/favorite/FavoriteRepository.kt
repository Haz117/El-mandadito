package com.mandadito.api.favorite

import org.springframework.data.jpa.repository.JpaRepository

interface FavoriteRepository : JpaRepository<Favorite, Long> {
    fun findByUserId(userId: Long): List<Favorite>
    fun findByUserIdAndRestaurantId(userId: Long, restaurantId: Long): Favorite?
    fun existsByUserIdAndRestaurantId(userId: Long, restaurantId: Long): Boolean
    fun deleteByUserIdAndRestaurantId(userId: Long, restaurantId: Long)
}
