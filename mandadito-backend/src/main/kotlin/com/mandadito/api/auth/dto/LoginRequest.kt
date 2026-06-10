package com.mandadito.api.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "El email es obligatorio")
    val email: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    val password: String
)
