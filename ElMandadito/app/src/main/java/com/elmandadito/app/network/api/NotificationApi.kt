package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.RegisterTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface NotificationApi {

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/device_tokens")
    suspend fun registerToken(@Body request: RegisterTokenRequest): Response<Unit>

    @DELETE("rest/v1/device_tokens")
    suspend fun unregisterToken(@Query("token") token: String): Response<Unit>
}
