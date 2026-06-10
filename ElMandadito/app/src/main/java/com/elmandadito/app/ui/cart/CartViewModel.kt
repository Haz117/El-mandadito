package com.elmandadito.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elmandadito.app.network.dto.CartResponse
import com.elmandadito.app.network.repository.OrderNetworkRepository
import com.elmandadito.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderNetworkRepository
) : ViewModel() {

    private val _cart = MutableStateFlow<UiState<CartResponse?>>(UiState.Loading)
    val cart: StateFlow<UiState<CartResponse?>> = _cart.asStateFlow()

    private val _orderResult = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val orderResult: StateFlow<UiState<Unit>> = _orderResult.asStateFlow()

    init { refreshCart() }

    fun refreshCart() {
        viewModelScope.launch {
            orderRepository.getCart().fold(
                onSuccess = { _cart.value = UiState.Success(it) },
                onFailure = { _cart.value = UiState.Error(it.message ?: "Error al cargar carrito") }
            )
        }
    }

    fun addItem(menuItemId: Long, quantity: Int = 1, notes: String? = null) {
        viewModelScope.launch {
            orderRepository.addToCart(menuItemId, quantity, notes).fold(
                onSuccess = { _cart.value = UiState.Success(it) },
                onFailure = { _cart.value = UiState.Error(it.message ?: "Error al agregar al carrito") }
            )
        }
    }

    fun removeItem(cartItemId: Long) {
        viewModelScope.launch {
            orderRepository.removeCartItem(cartItemId).fold(
                onSuccess = { _cart.value = UiState.Success(it) },
                onFailure = { _cart.value = UiState.Error(it.message ?: "Error al eliminar del carrito") }
            )
        }
    }

    fun placeOrder(addressId: Long, paymentMethod: String = "CASH", notes: String? = null) {
        _orderResult.value = UiState.Loading
        viewModelScope.launch {
            orderRepository.createOrder(addressId, paymentMethod, notes).fold(
                onSuccess = { _orderResult.value = UiState.Success(Unit) },
                onFailure = { _orderResult.value = UiState.Error(it.message ?: "Error al crear pedido") }
            )
        }
    }

    fun resetOrderResult() {
        _orderResult.value = UiState.Idle
    }
}
