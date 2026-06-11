package com.elmandadito.app.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/v1/signup")
    suspend fun register(
        @Body request: SupabaseSignUpRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body request: SupabaseLoginRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String
    ): Response<Unit>

    @POST("auth/v1/recover")
    suspend fun recoverPassword(
        @Body request: RecoverPasswordRequest
    ): Response<Unit>
}

// ─── Request bodies ───────────────────────────────────────────────────────────

data class RecoverPasswordRequest(val email: String)

data class SupabaseSignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String> = emptyMap()   // ej: {"name": "Juan"}
)

data class SupabaseLoginRequest(
    val email: String,
    val password: String
)

// ─── Response ─────────────────────────────────────────────────────────────────

data class SupabaseAuthResponse(
    val accessToken: String,     // Gson convierte access_token → accessToken con la policy
    val tokenType: String,
    val expiresIn: Int,
    val refreshToken: String,
    val user: SupabaseUser
)

data class SupabaseUser(
    val id: String,              // UUID del usuario
    val email: String,
    val userMetadata: Map<String, Any?> = emptyMap()
)
