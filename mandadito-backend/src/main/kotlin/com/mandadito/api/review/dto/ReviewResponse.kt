package com.mandadito.api.review.dto

import com.mandadito.api.review.Review
import java.time.LocalDateTime

data class ReviewResponse(
    val id: Long,
    val orderId: Long,
    val userId: Long,
    val restaurantId: Long,
    val driverId: Long?,
    val restaurantRating: Int,
    val driverRating: Int?,
    val comment: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(r: Review) = ReviewResponse(
            id = r.id,
            orderId = r.orderId,
            userId = r.userId,
            restaurantId = r.restaurantId,
            driverId = r.driverId,
            restaurantRating = r.restaurantRating,
            driverRating = r.driverRating,
            comment = r.comment,
            createdAt = r.createdAt
        )
    }
}
