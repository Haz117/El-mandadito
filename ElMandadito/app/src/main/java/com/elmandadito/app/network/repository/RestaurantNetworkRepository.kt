package com.elmandadito.app.network.repository

import com.elmandadito.app.network.RetrofitClient
import com.elmandadito.app.network.api.RestaurantApi
import com.elmandadito.app.network.dto.MenuItemResponse
import com.elmandadito.app.network.dto.RestaurantResponse

class RestaurantNetworkRepository(private val tokenProvider: () -> String?) {

    private val api: RestaurantApi by lazy {
        RetrofitClient.build(tokenProvider).create(RestaurantApi::class.java)
    }

    suspend fun getAll(category: String? = null, search: String? = null): Result<List<RestaurantResponse>> = runCatching {
        val response = api.getAll(category, search)
        response.body()?.data ?: emptyList()
    }

    suspend fun getById(id: Long): Result<RestaurantResponse> = runCatching {
        val response = api.getById(id)
        response.body()?.data ?: throw Exception("Restaurante no encontrado")
    }

    suspend fun getNearby(lat: Double, lng: Double): Result<List<RestaurantResponse>> = runCatching {
        val response = api.getNearby(lat, lng)
        response.body()?.data ?: emptyList()
    }

    suspend fun getMenu(restaurantId: Long): Result<List<MenuItemResponse>> = runCatching {
        val response = api.getMenu(restaurantId)
        response.body()?.data ?: emptyList()
    }
}
