package com.mandadito.api.notification.dto

import jakarta.validation.constraints.NotBlank

data class RegisterTokenRequest(
    @field:NotBlank(message = "El token es obligatorio")
    val token: String,

    val platform: String = "ANDROID"
)
