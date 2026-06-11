package com.elmandadito.app.network

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val tokenProvider: () -> String?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val userToken = tokenProvider()
        // Si hay JWT del usuario lo usa; si no, usa la anon key para acceso público
        val bearer = if (!userToken.isNullOrBlank()) userToken else SupabaseConfig.ANON_KEY

        val request = chain.request().newBuilder()
            .header("apikey", SupabaseConfig.ANON_KEY)
            .header("Authorization", "Bearer $bearer")
            .header("Content-Type", "application/json")
            .build()

        val response = chain.proceed(request)
        if (response.code == 401) SessionManager.notifyExpired()
        return response
    }
}
