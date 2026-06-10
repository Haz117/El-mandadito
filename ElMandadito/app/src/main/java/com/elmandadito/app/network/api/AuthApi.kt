package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.AuthResponse
import com.elmandadito.app.network.dto.LoginRequest
import com.elmandadito.app.network.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @GET("api/auth/me")
    suspend fun me(): Response<ApiResponse<Any>>
}

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)
