package com.elmandadito.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elmandadito.app.network.dto.AuthResponse
import com.elmandadito.app.network.repository.AuthRepository
import com.elmandadito.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val state: StateFlow<UiState<AuthResponse>> = _state.asStateFlow()

    fun login(email: String, password: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password).fold(
                onSuccess = { _state.value = UiState.Success(it) },
                onFailure = { _state.value = UiState.Error(it.message ?: "Error de inicio de sesión") }
            )
        }
    }

    fun register(name: String, email: String, password: String, phone: String? = null) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            authRepository.register(name, email, password, phone).fold(
                onSuccess = { _state.value = UiState.Success(it) },
                onFailure = { _state.value = UiState.Error(it.message ?: "Error al registrarse") }
            )
        }
    }

    fun resetState() {
        _state.value = UiState.Idle
    }
}
