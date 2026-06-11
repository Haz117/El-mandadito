package com.elmandadito.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elmandadito.app.data.CartItem
import com.elmandadito.app.network.dto.OrderItemResponse
import com.elmandadito.app.network.dto.OrderResponse
import com.elmandadito.app.network.dto.SupabaseCreateOrderRequest
import com.elmandadito.app.network.repository.AuthRepository
import com.elmandadito.app.network.repository.OrderNetworkRepository
import com.elmandadito.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderNetworkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _orderResult = MutableStateFlow<UiState<OrderResponse>>(UiState.Idle)
    val orderResult: StateFlow<UiState<OrderResponse>> = _orderResult.asStateFlow()

    fun placeOrder(
        networkRestaurantId: Long,
        subtotal: Double,
        deliveryFee: Double,
        discount: Double,
        total: Double,
        paymentMethod: String,
        notes: String?,
        address: String?,
        cartItems: List<CartItem> = emptyList()
    ) {
        _orderResult.value = UiState.Loading
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            if (userId == null) {
                _orderResult.value = UiState.Error("Necesitas iniciar sesión para realizar pedidos")
                return@launch
            }

            // Restaurante local (sin ID de Supabase) → crear pedido local sin llamada de red
            if (networkRestaurantId == 0L) {
                val fakeOrder = buildLocalOrder(userId, subtotal, deliveryFee, discount, total, paymentMethod, notes, address, cartItems)
                _orderResult.value = UiState.Success(fakeOrder)
                return@launch
            }

            val request = SupabaseCreateOrderRequest(
                userId        = userId,
                restaurantId  = networkRestaurantId,
                subtotal      = subtotal,
                deliveryFee   = deliveryFee,
                discount      = discount,
                total         = total,
                paymentMethod = paymentMethod.uppercase(),
                notes         = notes?.ifBlank { null },
                address       = address?.ifBlank { null }
            )
            orderRepository.createOrder(request, cartItems).fold(
                onSuccess = { _orderResult.value = UiState.Success(it) },
                onFailure = { _orderResult.value = UiState.Error(it.message ?: "Error al crear pedido") }
            )
        }
    }

    fun resetOrderResult() {
        _orderResult.value = UiState.Idle
    }

    private fun buildLocalOrder(
        userId: String, subtotal: Double, deliveryFee: Double,
        discount: Double, total: Double, paymentMethod: String,
        notes: String?, address: String?, cartItems: List<CartItem>
    ): OrderResponse {
        val fakeId = System.currentTimeMillis() % 99999
        val items = cartItems.mapIndexed { i, ci ->
            OrderItemResponse(
                id         = i.toLong() + 1,
                orderId    = fakeId,
                menuItemId = ci.menuItem.id.toLong(),
                name       = ci.menuItem.name,
                price      = ci.menuItem.price.toDouble(),
                quantity   = ci.quantity,
                subtotal   = ci.totalPrice.toDouble()
            )
        }
        return OrderResponse(
            id            = fakeId,
            userId        = userId,
            restaurantId  = 0L,
            status        = "PENDING",
            subtotal      = subtotal,
            deliveryFee   = deliveryFee,
            discount      = discount,
            total         = total,
            paymentMethod = paymentMethod.uppercase(),
            paymentStatus = "PENDING",
            notes         = notes,
            address       = address,
            createdAt     = Instant.now().toString(),
            orderItems    = items
        )
    }
}
