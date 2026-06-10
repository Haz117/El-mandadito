package com.mandadito.api.notification

import com.mandadito.api.notification.dto.RegisterTokenRequest
import com.mandadito.api.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun registerToken(request: RegisterTokenRequest) {
        val user = userService.getCurrentUser()

        deviceTokenRepository.findByToken(request.token).ifPresentOrElse({ existing ->
            existing.lastUsedAt = LocalDateTime.now()
            deviceTokenRepository.save(existing)
        }, {
            deviceTokenRepository.save(
                DeviceToken(
                    userId = user.id,
                    token = request.token,
                    platform = request.platform
                )
            )
        })
    }

    @Transactional
    fun unregisterToken(token: String) {
        deviceTokenRepository.deleteByToken(token)
    }

    fun sendOrderStatusNotification(userId: Long, orderId: Long, status: String) {
        val tokens = deviceTokenRepository.findByUserId(userId).map { it.token }
        if (tokens.isEmpty()) return

        val title = "Mandadito — Tu pedido #$orderId"
        val body = when (status) {
            "ACCEPTED_BY_RESTAURANT" -> "Tu pedido fue aceptado por el restaurante"
            "PREPARING"              -> "Tu pedido está siendo preparado"
            "READY_FOR_PICKUP"       -> "Tu pedido está listo para ser recogido"
            "DRIVER_ASSIGNED"        -> "Un repartidor fue asignado a tu pedido"
            "ON_THE_WAY"             -> "Tu pedido está en camino"
            "DELIVERED"              -> "Tu pedido fue entregado. ¡Buen provecho!"
            "CANCELLED"              -> "Tu pedido fue cancelado"
            "REJECTED"               -> "Tu pedido fue rechazado por el restaurante"
            else                     -> "Tu pedido actualizó su estado a $status"
        }

        // TODO: integrate with Firebase Admin SDK
        // FirebaseMessaging.getInstance().sendEachForMulticast(...)
        log.info("NOTIFICATION → userId=$userId tokens=${tokens.size} title='$title' body='$body'")
    }
}
