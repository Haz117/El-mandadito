package com.elmandadito.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class HomeViewModel @Inject constructor(
    private val restaurantRepository: RestaurantNetworkRepository
) : ViewModel() {

    private val _restaurants = MutableStateFlow<UiState<List<RestaurantResponse>>>(UiState.Loading)
    val restaurants: StateFlow<UiState<List<RestaurantResponse>>> = _restaurants.asStateFlow()

    init { loadRestaurants() }

    fun loadRestaurants(category: String? = null, search: String? = null) {
        _restaurants.value = UiState.Loading
        viewModelScope.launch {
            restaurantRepository.getAll(category, search).fold(
                onSuccess = { _restaurants.value = UiState.Success(it) },
                onFailure = { _restaurants.value = UiState.Error(it.message ?: "Error al cargar restaurantes") }
            )
        }
    }
}
