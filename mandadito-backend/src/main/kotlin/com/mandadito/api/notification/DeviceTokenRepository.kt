package com.mandadito.api.notification

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface DeviceTokenRepository : JpaRepository<DeviceToken, Long> {
    fun findByUserId(userId: Long): List<DeviceToken>
    fun findByToken(token: String): Optional<DeviceToken>
    fun deleteByToken(token: String)
}
