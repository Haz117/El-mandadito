package com.elmandadito.app.network.repository

import com.elmandadito.app.network.RetrofitClient
import com.elmandadito.app.network.api.OrderApi
import com.elmandadito.app.network.dto.AddCartItemRequest
import com.elmandadito.app.network.dto.CartResponse
import com.elmandadito.app.network.dto.CreateOrderRequest
import com.elmandadito.app.network.dto.OrderResponse

class OrderNetworkRepository(private val tokenProvider: () -> String?) {

    private val api: OrderApi by lazy {
        RetrofitClient.build(tokenProvider).create(OrderApi::class.java)
    }

    // ─── Cart ─────────────────────────────────────────────────────────────────

    suspend fun getCart(): Result<CartResponse?> = runCatching {
        api.getCart().body()?.data
    }

    suspend fun addToCart(menuItemId: Long, quantity: Int = 1, notes: String? = null): Result<CartResponse?> = runCatching {
        val response = api.addToCart(AddCartItemRequest(menuItemId, quantity, notes))
        if (response.isSuccessful) response.body()?.data
        else throw Exception(response.body()?.message ?: "Error al agregar al carrito")
    }

    suspend fun removeCartItem(cartItemId: Long): Result<CartResponse?> = runCatching {
        api.removeCartItem(cartItemId).body()?.data
    }

    suspend fun clearCart(): Result<Unit> = runCatching {
        api.clearCart()
    }

    // ─── Orders ───────────────────────────────────────────────────────────────

    suspend fun createOrder(addressId: Long, paymentMethod: String = "CASH", notes: String? = null): Result<OrderResponse> = runCatching {
        val response = api.createOrder(CreateOrderRequest(addressId, paymentMethod, notes))
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()!!.data!!
        } else {
            throw Exception(response.body()?.message ?: "Error al crear pedido")
        }
    }

    suspend fun getMyOrders(): Result<List<OrderResponse>> = runCatching {
        api.getMyOrders().body()?.data ?: emptyList()
    }

    suspend fun getOrderById(id: Long): Result<OrderResponse> = runCatching {
        api.getOrderById(id).body()?.data ?: throw Exception("Pedido no encontrado")
    }

    suspend fun cancelOrder(id: Long): Result<OrderResponse> = runCatching {
        val response = api.cancelOrder(id)
        response.body()?.data ?: throw Exception("Error al cancelar pedido")
    }
}
