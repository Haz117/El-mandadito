package com.mandadito.api.order

import jakarta.persistence.*

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column
    val menuItemId: Long? = null,

    @Column(nullable = false, length = 200)
    val nameSnapshot: String,

    @Column(nullable = false)
    val priceSnapshot: Double,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false)
    val subtotal: Double,

    @Column(length = 300)
    val notes: String? = null
)
