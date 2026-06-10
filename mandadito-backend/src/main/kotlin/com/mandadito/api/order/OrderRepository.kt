package com.mandadito.api.order

import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>
    fun findByRestaurantIdOrderByCreatedAtDesc(restaurantId: Long): List<Order>
    fun findByDriverIdOrderByCreatedAtDesc(driverId: Long): List<Order>
    fun findByRestaurantIdAndStatus(restaurantId: Long, status: OrderStatus): List<Order>
}
