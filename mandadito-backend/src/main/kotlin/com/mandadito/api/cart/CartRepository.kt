package com.mandadito.api.cart

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CartRepository : JpaRepository<Cart, Long> {
    fun findByUserId(userId: Long): Optional<Cart>
}
