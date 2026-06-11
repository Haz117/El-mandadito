package com.elmandadito.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elmandadito.app.data.OrderRecord
import com.elmandadito.app.network.repository.AuthRepository
import com.elmandadito.app.network.repository.OrderNetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderNetworkRepository
) : ViewModel() {

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    private val _networkOrders = MutableStateFlow<List<OrderRecord>>(emptyList())
    val networkOrders: StateFlow<List<OrderRecord>> = _networkOrders.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loggedOut.value = true
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            orderRepository.getMyOrders(userId).onSuccess { orders ->
                val fmt = SimpleDateFormat("dd MMM", Locale("es", "MX"))
                _networkOrders.value = orders.map { o ->
                    OrderRecord(
                        restaurantName = "Pedido #${o.id}",
                        total          = o.total.toInt(),
                        itemCount      = o.orderItems.size.coerceAtLeast(1),
                        date           = runCatching {
                            fmt.format(java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .parse(o.createdAt.take(10))!!)
                        }.getOrElse { o.createdAt.take(10) },
                        paymentMethod  = when (o.paymentMethod.uppercase()) {
                            "CARD"  -> "Tarjeta"
                            "OXXO"  -> "OXXO"
                            else    -> "Efectivo"
                        },
                        networkOrderId = o.id
                    )
                }
            }
        }
    }
}
