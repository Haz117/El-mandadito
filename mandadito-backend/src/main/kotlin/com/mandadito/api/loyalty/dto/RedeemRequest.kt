package com.mandadito.api.loyalty.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class RedeemRequest(
    @field:NotNull @field:Min(value = 1, message = "Debes canjear al menos 1 punto")
    val points: Int
)
