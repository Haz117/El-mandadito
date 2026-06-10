package com.mandadito.api.business.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateBusinessRequest(
    @field:NotBlank(message = "El nombre del negocio es obligatorio")
    @field:Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    val name: String,

    @field:Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    val description: String? = null,

    val phone: String? = null,

    @field:Email(message = "Email inválido")
    val email: String? = null,

    val address: String? = null,
    val city: String? = null
)
