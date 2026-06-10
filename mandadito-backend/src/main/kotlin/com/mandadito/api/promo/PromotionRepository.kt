package com.mandadito.api.promo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface PromotionRepository : JpaRepository<Promotion, Long> {

    @Query("""
        SELECT p FROM Promotion p
        WHERE p.active = true
        AND (p.startDate IS NULL OR p.startDate <= :today)
        AND (p.endDate IS NULL OR p.endDate >= :today)
        ORDER BY p.createdAt DESC
    """)
    fun findActivePromotions(today: LocalDate): List<Promotion>
}
