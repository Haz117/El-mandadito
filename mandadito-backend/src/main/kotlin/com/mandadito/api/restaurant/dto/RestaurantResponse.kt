package com.mandadito.api.restaurant.dto

import com.mandadito.api.restaurant.Restaurant
import com.mandadito.api.restaurant.RestaurantStatus
import java.time.LocalDateTime

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
    val status: RestaurantStatus,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(r: Restaurant) = RestaurantResponse(
            id = r.id,
            businessId = r.businessId,
            name = r.name,
            description = r.description,
            category = r.category,
            imageUrl = r.imageUrl,
            coverImageUrl = r.coverImageUrl,
            rating = r.rating,
            totalRatings = r.totalRatings,
            deliveryTimeMin = r.deliveryTimeMin,
            deliveryTimeMax = r.deliveryTimeMax,
            deliveryFee = r.deliveryFee,
            isOpen = r.isOpen,
            status = r.status,
            latitude = r.latitude,
            longitude = r.longitude,
            createdAt = r.createdAt
        )
    }
}
