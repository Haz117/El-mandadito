package com.mandadito.api.cart

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "carts")
class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val userId: Long,

    @Column
    var restaurantId: Long? = null,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val items: MutableList<CartItem> = mutableListOf(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
