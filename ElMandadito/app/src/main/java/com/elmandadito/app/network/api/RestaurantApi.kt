package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.MenuItemResponse
import com.elmandadito.app.network.dto.RestaurantResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantApi {

    @GET("api/restaurants")
    suspend fun getAll(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<RestaurantResponse>>>

    @GET("api/restaurants/{id}")
    suspend fun getById(@Path("id") id: Long): Response<ApiResponse<RestaurantResponse>>

    @GET("api/restaurants/nearby")
    suspend fun getNearby(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusKm") radiusKm: Double = 10.0
    ): Response<ApiResponse<List<RestaurantResponse>>>

    @GET("api/restaurants/{restaurantId}/menu")
    suspend fun getMenu(@Path("restaurantId") restaurantId: Long): Response<ApiResponse<List<MenuItemResponse>>>

    @GET("api/restaurants/{restaurantId}/reviews")
    suspend fun getReviews(@Path("restaurantId") restaurantId: Long): Response<ApiResponse<List<Any>>>
}
