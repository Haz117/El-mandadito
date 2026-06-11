package com.elmandadito.app.network.repository

import com.elmandadito.app.data.CartItem
import com.elmandadito.app.network.RetrofitClient
import com.elmandadito.app.network.api.OrderApi
import com.elmandadito.app.network.dto.OrderItemInsertRequest
import com.elmandadito.app.network.dto.OrderResponse
import com.elmandadito.app.network.dto.SupabaseCreateOrderRequest
import java.io.IOException
import java.net.SocketTimeoutException

class OrderNetworkRepository(private val tokenProvider: () -> String?) {

    private val api: OrderApi by lazy {
        RetrofitClient.build(tokenProvider).create(OrderApi::class.java)
    }

    suspend fun getMyOrders(userId: String): Result<List<OrderResponse>> = safe {
        api.getMyOrders(userId = "eq.$userId").body() ?: emptyList()
    }

    suspend fun createOrder(
        request: SupabaseCreateOrderRequest,
        cartItems: List<CartItem> = emptyList()
    ): Result<OrderResponse> = safe {
        val response = api.createOrder(request)
        val order = if (response.isSuccessful) {
            response.body()?.firstOrNull() ?: throw Exception("No se recibió confirmación del pedido")
        } else {
            throw Exception("Error al crear pedido (${response.code()})")
        }
        // Inserta los items del pedido si se proporcionaron
        if (cartItems.isNotEmpty()) {
            val itemRequests = cartItems.map { item ->
                OrderItemInsertRequest(
                    orderId    = order.id,
                    menuItemId = if (item.menuItem.id > 0) item.menuItem.id.toLong() else null,
                    name       = item.menuItem.name,
                    price      = item.menuItem.price.toDouble(),
                    quantity   = item.quantity,
                    subtotal   = item.totalPrice.toDouble()
                )
            }
            runCatching { api.createOrderItems(itemRequests) }
        }
        order
    }

    suspend fun getOrderById(id: Long): Result<OrderResponse> = safe {
        api.getOrderById("eq.$id").body()?.firstOrNull()
            ?: throw Exception("Pedido no encontrado")
    }

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
