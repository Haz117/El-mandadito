package com.mandadito.api.loyalty

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "loyalty_points")
class LoyaltyPoint(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val points: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: LoyaltyPointType,

    @Column(length = 200)
    val description: String? = null,

    @Column
    val orderId: Long? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
