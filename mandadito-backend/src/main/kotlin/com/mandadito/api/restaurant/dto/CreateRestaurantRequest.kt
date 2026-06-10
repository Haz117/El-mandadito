package com.mandadito.api.restaurant.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateRestaurantRequest(
    @field:NotBlank(message = "El nombre del restaurante es obligatorio")
    @field:Size(max = 200)
    val name: String,

    @field:Size(max = 1000)
    val description: String? = null,

    val category: String? = null,
    val imageUrl: String? = null,
    val coverImageUrl: String? = null,

    @field:Min(value = 1, message = "El tiempo mínimo debe ser mayor a 0")
    val deliveryTimeMin: Int = 20,

    @field:Min(value = 1, message = "El tiempo máximo debe ser mayor a 0")
    val deliveryTimeMax: Int = 40,

    val deliveryFee: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null
)
