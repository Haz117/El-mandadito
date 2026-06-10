package com.elmandadito.app.network.repository

import com.elmandadito.app.network.RetrofitClient
import com.elmandadito.app.network.api.NotificationApi
import com.elmandadito.app.network.dto.RegisterTokenRequest

class NotificationNetworkRepository(private val tokenProvider: () -> String?) {

    private val api: NotificationApi by lazy {
        RetrofitClient.build(tokenProvider).create(NotificationApi::class.java)
    }

    suspend fun registerToken(fcmToken: String): Result<Unit> = runCatching {
        api.registerToken(RegisterTokenRequest(fcmToken))
    }

    suspend fun unregisterToken(fcmToken: String): Result<Unit> = runCatching {
        api.unregisterToken(fcmToken)
    }
}
