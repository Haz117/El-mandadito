package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.RegisterTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @POST("api/devices/register-token")
    suspend fun registerToken(@Body request: RegisterTokenRequest): Response<ApiResponse<Nothing?>>

    @DELETE("api/devices/{token}")
    suspend fun unregisterToken(@Path("token") token: String): Response<ApiResponse<Nothing?>>
}
