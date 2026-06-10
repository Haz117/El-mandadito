package com.mandadito.api.tracking

import org.springframework.data.jpa.repository.JpaRepository

interface OrderEventRepository : JpaRepository<OrderEvent, Long> {
    fun findByOrderIdOrderByCreatedAtAsc(orderId: Long): List<OrderEvent>
    fun findTopByOrderIdOrderByCreatedAtDesc(orderId: Long): OrderEvent?
}
