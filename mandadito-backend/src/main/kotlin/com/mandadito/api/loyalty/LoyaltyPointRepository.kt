package com.mandadito.api.loyalty

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LoyaltyPointRepository : JpaRepository<LoyaltyPoint, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<LoyaltyPoint>

    @Query("SELECT COALESCE(SUM(CASE WHEN l.type = 'EARNED' OR l.type = 'ADJUSTED' THEN l.points WHEN l.type = 'REDEEMED' OR l.type = 'EXPIRED' THEN -l.points ELSE 0 END), 0) FROM LoyaltyPoint l WHERE l.userId = :userId")
    fun getTotalPointsByUserId(userId: Long): Int
}
