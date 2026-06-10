package com.mandadito.api.user.dto

data class UserStatsResponse(
    val totalOrders: Long,
    val deliveredOrders: Long,
    val cancelledOrders: Long,
    val totalSpent: Double,
    val mandapoints: Int,
    val rating: Double,
    val favoritesCount: Long
)
