package com.elmandadito.app.network.repository

import com.elmandadito.app.network.RetrofitClient
import com.elmandadito.app.network.api.ReviewApi
import com.elmandadito.app.network.dto.CreateReviewRequest

class ReviewNetworkRepository(private val tokenProvider: () -> String?) {

    private val api: ReviewApi by lazy {
        RetrofitClient.build(tokenProvider).create(ReviewApi::class.java)
    }

    suspend fun create(orderId: Long, restaurantRating: Int, comment: String?): Result<Unit> = runCatching {
        val response = api.createReview(CreateReviewRequest(orderId, restaurantRating, null, comment))
        if (!response.isSuccessful) throw Exception("Error al enviar calificación")
    }
}
