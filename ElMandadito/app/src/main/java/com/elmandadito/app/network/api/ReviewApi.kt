package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.CreateReviewRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ReviewApi {

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<Unit>
}
