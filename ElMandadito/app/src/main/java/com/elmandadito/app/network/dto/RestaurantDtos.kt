package com.elmandadito.app.network.dto

data class RestaurantResponse(
    val id: Long,
    val businessId: Long,
    val name: String,
    val description: String?,
    val category: String?,
    val imageUrl: String?,
    val coverImageUrl: String?,
    val rating: Double,
    val totalRatings: Int,
    val deliveryTimeMin: Int,
    val deliveryTimeMax: Int,
    val deliveryFee: Double,
    val isOpen: Boolean,
    val status: String,
    val latitude: Double?,
    val longitude: Double?
)

data class MenuItemResponse(
    val id: Long,
    val restaurantId: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val imageUrl: String?,
    val category: String?,
    val available: Boolean
)
