package com.mandadito.api.restaurant

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "restaurants")
class Restaurant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val businessId: Long,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(length = 1000)
    var description: String? = null,

    @Column(length = 50)
    var category: String? = null,

    @Column(length = 500)
    var imageUrl: String? = null,

    @Column(length = 500)
    var coverImageUrl: String? = null,

    @Column(nullable = false)
    var rating: Double = 0.0,

    @Column(nullable = false)
    var totalRatings: Int = 0,

    @Column(nullable = false)
    var deliveryTimeMin: Int = 20,

    @Column(nullable = false)
    var deliveryTimeMax: Int = 40,

    @Column(nullable = false)
    var deliveryFee: Double = 0.0,

    @Column(nullable = false)
    var isOpen: Boolean = true,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: RestaurantStatus = RestaurantStatus.ACTIVE,

    @Column
    var latitude: Double? = null,

    @Column
    var longitude: Double? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
