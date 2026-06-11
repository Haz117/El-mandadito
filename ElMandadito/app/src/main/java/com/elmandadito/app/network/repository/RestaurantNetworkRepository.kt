package com.elmandadito.app.network.repository

import com.elmandadito.app.network.RetrofitClient
import com.elmandadito.app.network.api.RestaurantApi
import com.elmandadito.app.network.dto.MenuItemResponse
import com.elmandadito.app.network.dto.RestaurantResponse
import java.io.IOException
import java.net.SocketTimeoutException

class RestaurantNetworkRepository(private val tokenProvider: () -> String?) {

    private val api: RestaurantApi by lazy {
        RetrofitClient.build(tokenProvider).create(RestaurantApi::class.java)
    }

    suspend fun getAll(category: String? = null, search: String? = null): Result<List<RestaurantResponse>> =
        safe { api.getAll(category = category?.let { "eq.$it" }).body() ?: emptyList() }

    suspend fun getById(id: Long): Result<RestaurantResponse> =
        safe {
            api.getById("eq.$id").body()?.firstOrNull()
                ?: throw Exception("Restaurante no encontrado")
        }

    suspend fun getMenu(restaurantId: Long): Result<List<MenuItemResponse>> =
        safe { api.getMenu("eq.$restaurantId").body() ?: emptyList() }

    suspend fun getNearby(lat: Double, lng: Double): Result<List<RestaurantResponse>> =
        safe { api.getAll().body() ?: emptyList() }

    private suspend fun <T> safe(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: SocketTimeoutException) {
        Result.failure(Exception("El servidor tardó demasiado en responder"))
    } catch (e: IOException) {
        Result.failure(Exception("Sin conexión a internet"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
