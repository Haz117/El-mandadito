package com.mandadito.api.promo.dto

import com.mandadito.api.promo.DiscountType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CreatePromotionRequest(
    @field:NotBlank(message = "El título es obligatorio")
    val title: String,

    val description: String? = null,
    val imageUrl: String? = null,

    @field:NotNull
    val discountType: DiscountType,

    @field:DecimalMin(value = "0.01", message = "El valor del descuento debe ser mayor a 0")
    val discountValue: Double,

    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val active: Boolean = true
)
