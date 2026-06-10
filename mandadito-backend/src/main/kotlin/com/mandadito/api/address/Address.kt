package com.mandadito.api.address

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "addresses")
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 200)
    var street: String,

    @Column(length = 20)
    var number: String? = null,

    @Column(length = 100)
    var neighborhood: String? = null,

    @Column(nullable = false, length = 100)
    var city: String,

    @Column(length = 100)
    var state: String? = null,

    @Column(length = 10)
    var zipCode: String? = null,

    @Column(length = 300)
    var reference: String? = null,

    @Column
    var latitude: Double? = null,

    @Column
    var longitude: Double? = null,

    @Column(nullable = false)
    var isDefault: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
