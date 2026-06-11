package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.MenuItemResponse
import com.elmandadito.app.network.dto.RestaurantResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RestaurantApi {

    // GET /rest/v1/restaurants?status=eq.ACTIVE&select=*
    @Headers("Prefer: count=none")
    @GET("rest/v1/restaurants")
    suspend fun getAll(
        @Query("category") category: String? = null,    // "eq.mexican"
        @Query("status")   status: String = "eq.ACTIVE",
        @Query("select")   select: String = "*"
    ): Response<List<RestaurantResponse>>

    // GET /rest/v1/restaurants?id=eq.5&select=*
    @GET("rest/v1/restaurants")
    suspend fun getById(
        @Query("id")     id: String,                    // "eq.{id}"
        @Query("select") select: String = "*"
    ): Response<List<RestaurantResponse>>

    // GET /rest/v1/menu_items?restaurant_id=eq.5&available=eq.true&select=*
    @GET("rest/v1/menu_items")
    suspend fun getMenu(
        @Query("restaurant_id") restaurantId: String,  // "eq.{id}"
        @Query("available")     available: String = "eq.true",
        @Query("select")        select: String = "*"
    ): Response<List<MenuItemResponse>>
}
