package com.mandadito.api.business.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateBusinessRequest(
    @field:NotBlank(message = "El nombre del negocio es obligatorio")
    @field:Size(max = 200)
    val name: String,

    @field:Size(max = 1000)
    val description: String? = null,

    val phone: String? = null,

    @field:Email(message = "Email inválido")
    val email: String? = null,

    val address: String? = null,
    val city: String? = null,
    val logoUrl: String? = null
)
