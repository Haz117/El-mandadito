package com.elmandadito.app.network.api

import com.elmandadito.app.network.dto.OrderResponse
import com.elmandadito.app.network.dto.SupabaseCreateOrderRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderApi {

    // GET /rest/v1/orders?user_id=eq.{uuid}&select=*,order_items(*)
    @GET("rest/v1/orders")
    suspend fun getMyOrders(
        @Query("user_id") userId: String,        // "eq.{uuid}"
        @Query("select")  select: String = "*,order_items(*)",
        @Query("order")   order: String = "created_at.desc"
    ): Response<List<OrderResponse>>

    // POST /rest/v1/orders
    @Headers("Prefer: return=representation")
    @POST("rest/v1/orders")
    suspend fun createOrder(
        @Body request: SupabaseCreateOrderRequest
    ): Response<List<OrderResponse>>
}
