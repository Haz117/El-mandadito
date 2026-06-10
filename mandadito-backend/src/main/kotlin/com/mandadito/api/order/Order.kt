package com.mandadito.api.order

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val restaurantId: Long,

    @Column
    var driverId: Long? = null,

    @Column(nullable = false)
    val addressId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: OrderStatus = OrderStatus.CREATED,

    @Column(nullable = false)
    val subtotal: Double,

    @Column(nullable = false)
    val deliveryFee: Double,

    @Column(nullable = false)
    val serviceFee: Double = 0.0,

    @Column(nullable = false)
    var discount: Double = 0.0,

    @Column(nullable = false)
    val total: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var paymentMethod: PaymentMethod = PaymentMethod.CASH,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @Column(length = 500)
    val notes: String? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val items: MutableList<OrderItem> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var deliveredAt: LocalDateTime? = null,

    @Column
    var cancelledAt: LocalDateTime? = null
)
