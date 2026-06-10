package com.mandadito.api.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateReviewRequest(
    @field:NotNull(message = "El ID del pedido es obligatorio")
    val orderId: Long,

    @field:NotNull(message = "La calificación del restaurante es obligatoria")
    @field:Min(value = 1, message = "La calificación mínima es 1")
    @field:Max(value = 5, message = "La calificación máxima es 5")
    val restaurantRating: Int,

    @field:Min(1) @field:Max(5)
    val driverRating: Int? = null,

    @field:Size(max = 1000, message = "El comentario no puede superar 1000 caracteres")
    val comment: String? = null
)
