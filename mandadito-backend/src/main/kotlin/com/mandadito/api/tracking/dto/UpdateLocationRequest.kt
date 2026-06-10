package com.mandadito.api.tracking.dto

import jakarta.validation.constraints.NotNull

data class UpdateLocationRequest(
    @field:NotNull val latitude: Double,
    @field:NotNull val longitude: Double,
    val note: String? = null
)
