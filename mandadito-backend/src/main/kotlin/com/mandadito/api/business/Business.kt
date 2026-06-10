package com.mandadito.api.business

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "businesses")
class Business(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val ownerId: Long,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(length = 1000)
    var description: String? = null,

    @Column(length = 500)
    var logoUrl: String? = null,

    @Column(length = 20)
    var phone: String? = null,

    @Column(length = 255)
    var email: String? = null,

    @Column(length = 300)
    var address: String? = null,

    @Column(length = 100)
    var city: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: BusinessStatus = BusinessStatus.PENDING,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
