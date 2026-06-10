package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.AddCartItemRequest
import com.elmandadito.app.network.dto.CartResponse
import com.elmandadito.app.network.dto.CreateOrderRequest
import com.elmandadito.app.network.dto.OrderResponse
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {

    // Cart
    @GET("api/cart")
    suspend fun getCart(): Response<ApiResponse<CartResponse>>

    @POST("api/cart/items")
    suspend fun addToCart(@Body request: AddCartItemRequest): Response<ApiResponse<CartResponse>>

    @PUT("api/cart/items/{id}")
    suspend fun updateCartItem(
        @Path("id") id: Long,
        @Query("quantity") quantity: Int
    ): Response<ApiResponse<CartResponse>>

    @DELETE("api/cart/items/{id}")
    suspend fun removeCartItem(@Path("id") id: Long): Response<ApiResponse<CartResponse>>

    @DELETE("api/cart")
    suspend fun clearCart(): Response<ApiResponse<Nothing>>

    // Orders
    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<OrderResponse>>

    @GET("api/orders")
    suspend fun getMyOrders(): Response<ApiResponse<List<OrderResponse>>>

    @GET("api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): Response<ApiResponse<OrderResponse>>

    @GET("api/orders/{id}/tracking")
    suspend fun getTracking(@Path("id") id: Long): Response<ApiResponse<Any>>

    @PATCH("api/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: Long): Response<ApiResponse<OrderResponse>>
}
