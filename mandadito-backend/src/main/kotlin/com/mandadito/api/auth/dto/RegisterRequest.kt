package com.mandadito.api.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    val name: String,

    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "El email es obligatorio")
    val email: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    val password: String,

    @field:Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Teléfono inválido")
    val phone: String? = null,

    val role: String = "USER"
)
