package com.mandadito.api.menu

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "menu_items")
class MenuItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val restaurantId: Long,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var price: Double,

    @Column(length = 500)
    var imageUrl: String? = null,

    @Column(length = 50)
    var category: String? = null,

    @Column(nullable = false)
    var available: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
