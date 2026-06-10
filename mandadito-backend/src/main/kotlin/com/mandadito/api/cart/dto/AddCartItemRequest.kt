package com.mandadito.api.cart.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class AddCartItemRequest(
    @field:NotNull(message = "El producto es obligatorio")
    val menuItemId: Long,

    @field:Min(value = 1, message = "La cantidad debe ser al menos 1")
    val quantity: Int = 1,

    val notes: String? = null
)
