package com.mandadito.api.notification

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "device_tokens",
    uniqueConstraints = [UniqueConstraint(columnNames = ["token"])]
)
class DeviceToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, unique = true, length = 500)
    val token: String,

    @Column(nullable = false, length = 20)
    val platform: String = "ANDROID",

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var lastUsedAt: LocalDateTime = LocalDateTime.now()
)
