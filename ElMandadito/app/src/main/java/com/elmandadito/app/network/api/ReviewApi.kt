package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.CreateReviewRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReviewApi {

    @POST("api/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<ApiResponse<Any>>
}
