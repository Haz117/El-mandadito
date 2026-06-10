package com.mandadito.api.cart

import jakarta.persistence.*

@Entity
@Table(name = "cart_items")
class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    val cart: Cart,

    @Column(nullable = false)
    val menuItemId: Long,

    @Column(nullable = false)
    var quantity: Int,

    @Column(length = 300)
    var notes: String? = null
)
