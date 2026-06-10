package com.elmandadito.app.network.dto

data class CreateReviewRequest(
    val orderId: Long,
    val restaurantRating: Int,
    val driverRating: Int? = null,
    val comment: String? = null
)
