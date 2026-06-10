package com.elmandadito.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elmandadito.app.network.dto.MenuItemResponse
import com.elmandadito.app.network.dto.RestaurantResponse
import com.elmandadito.app.network.repository.RestaurantNetworkRepository
import com.elmandadito.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    private val repository: RestaurantNetworkRepository
) : ViewModel() {

    private val _restaurant = MutableStateFlow<UiState<RestaurantResponse>>(UiState.Loading)
    val restaurant: StateFlow<UiState<RestaurantResponse>> = _restaurant.asStateFlow()

    private val _menu = MutableStateFlow<UiState<List<MenuItemResponse>>>(UiState.Loading)
    val menu: StateFlow<UiState<List<MenuItemResponse>>> = _menu.asStateFlow()

    fun load(restaurantId: Long) {
        viewModelScope.launch {
            launch {
                repository.getById(restaurantId).fold(
                    onSuccess = { _restaurant.value = UiState.Success(it) },
                    onFailure = { _restaurant.value = UiState.Error(it.message ?: "Error al cargar restaurante") }
                )
            }
            launch {
                repository.getMenu(restaurantId).fold(
                    onSuccess = { _menu.value = UiState.Success(it) },
                    onFailure = { _menu.value = UiState.Error(it.message ?: "Error al cargar menú") }
                )
            }
        }
    }
}
