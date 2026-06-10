package com.mandadito.api.review

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = [UniqueConstraint(columnNames = ["order_id", "user_id"])]
)
class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = false)
    val orderId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val restaurantId: Long,

    @Column
    val driverId: Long? = null,

    @Column(nullable = false)
    val restaurantRating: Int,

    @Column
    val driverRating: Int? = null,

    @Column(length = 1000)
    val comment: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
