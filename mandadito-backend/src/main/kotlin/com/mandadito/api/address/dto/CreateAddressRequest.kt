package com.mandadito.api.address.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateAddressRequest(
    @field:NotBlank(message = "La calle es obligatoria")
    @field:Size(max = 200)
    val street: String,

    @field:Size(max = 20)
    val number: String? = null,

    @field:Size(max = 100)
    val neighborhood: String? = null,

    @field:NotBlank(message = "La ciudad es obligatoria")
    @field:Size(max = 100)
    val city: String,

    @field:Size(max = 100)
    val state: String? = null,

    @field:Size(max = 10)
    val zipCode: String? = null,

    @field:Size(max = 300)
    val reference: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDefault: Boolean = false
)
