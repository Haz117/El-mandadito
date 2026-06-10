package com.mandadito.api.promo.dto

import com.mandadito.api.promo.DiscountType
import com.mandadito.api.promo.Promotion
import java.time.LocalDate
import java.time.LocalDateTime

data class PromotionResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val discountType: DiscountType,
    val discountValue: Double,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val active: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(p: Promotion) = PromotionResponse(
            id = p.id,
            title = p.title,
            description = p.description,
            imageUrl = p.imageUrl,
            discountType = p.discountType,
            discountValue = p.discountValue,
            startDate = p.startDate,
            endDate = p.endDate,
            active = p.active,
            createdAt = p.createdAt
        )
    }
}
