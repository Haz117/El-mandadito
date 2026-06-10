package com.mandadito.api.loyalty.dto

import com.mandadito.api.loyalty.LoyaltyPoint
import com.mandadito.api.loyalty.LoyaltyPointType
import java.time.LocalDateTime

data class LoyaltyResponse(
    val totalPoints: Int,
    val level: String,
    val history: List<LoyaltyHistoryDto>
) {
    data class LoyaltyHistoryDto(
        val id: Long,
        val points: Int,
        val type: LoyaltyPointType,
        val description: String?,
        val orderId: Long?,
        val createdAt: LocalDateTime
    ) {
        companion object {
            fun from(lp: LoyaltyPoint) = LoyaltyHistoryDto(lp.id, lp.points, lp.type, lp.description, lp.orderId, lp.createdAt)
        }
    }

    companion object {
        fun levelFor(points: Int) = when {
            points >= 5000 -> "PLATINUM"
            points >= 2000 -> "GOLD"
            points >= 500  -> "SILVER"
            else           -> "BRONZE"
        }
    }
}
