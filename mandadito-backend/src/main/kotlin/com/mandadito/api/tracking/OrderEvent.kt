package com.mandadito.api.tracking

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "order_events")
class OrderEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val orderId: Long,

    @Column(nullable = false, length = 30)
    val status: String,

    @Column
    val latitude: Double? = null,

    @Column
    val longitude: Double? = null,

    @Column(length = 300)
    val note: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
