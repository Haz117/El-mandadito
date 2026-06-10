package com.mandadito.api.address

import org.springframework.data.jpa.repository.JpaRepository

interface AddressRepository : JpaRepository<Address, Long> {
    fun findByUserId(userId: Long): List<Address>
    fun findByUserIdAndIsDefault(userId: Long, isDefault: Boolean): Address?
}
