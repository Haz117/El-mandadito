package com.mandadito.api.review

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByRestaurantIdOrderByCreatedAtDesc(restaurantId: Long): List<Review>
    fun findByDriverIdOrderByCreatedAtDesc(driverId: Long): List<Review>
    fun existsByOrderIdAndUserId(orderId: Long, userId: Long): Boolean

    @Query("SELECT AVG(r.restaurantRating) FROM Review r WHERE r.restaurantId = :restaurantId")
    fun getAverageRating(restaurantId: Long): Double?

    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurantId = :restaurantId")
    fun countByRestaurantId(restaurantId: Long): Long
}
