package com.mandadito.api.menu.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateMenuItemRequest(
    @field:NotBlank(message = "El nombre del producto es obligatorio")
    @field:Size(max = 200)
    val name: String,

    @field:Size(max = 500)
    val description: String? = null,

    @field:DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    val price: Double,

    val imageUrl: String? = null,
    val category: String? = null,
    val available: Boolean = true
)
