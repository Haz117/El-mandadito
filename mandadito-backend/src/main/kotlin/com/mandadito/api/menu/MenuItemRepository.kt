package com.mandadito.api.menu

import org.springframework.data.jpa.repository.JpaRepository

interface MenuItemRepository : JpaRepository<MenuItem, Long> {
    fun findByRestaurantId(restaurantId: Long): List<MenuItem>
    fun findByRestaurantIdAndAvailable(restaurantId: Long, available: Boolean): List<MenuItem>
}
