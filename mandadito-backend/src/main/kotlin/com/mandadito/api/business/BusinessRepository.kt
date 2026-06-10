package com.mandadito.api.business

import org.springframework.data.jpa.repository.JpaRepository

interface BusinessRepository : JpaRepository<Business, Long> {
    fun findByOwnerId(ownerId: Long): List<Business>
    fun findByStatus(status: BusinessStatus): List<Business>
}
