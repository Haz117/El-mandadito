package com.mandadito.api.tracking

import com.mandadito.api.order.OrderRepository
import com.mandadito.api.tracking.dto.TrackingResponse
import com.mandadito.api.tracking.dto.UpdateLocationRequest
import com.mandadito.api.user.UserService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TrackingService(
    private val orderEventRepository: OrderEventRepository,
    private val orderRepository: OrderRepository,
    private val userService: UserService
) {

    fun getTracking(orderId: Long): TrackingResponse {
        val user = userService.getCurrentUser()
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Pedido $orderId no encontrado") }
        if (order.userId != user.id && order.driverId != user.id) {
            throw AccessDeniedException("No tienes acceso al tracking de este pedido")
        }

        val events = orderEventRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
        val latest = orderEventRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)

        return TrackingResponse(
            orderId = orderId,
            currentStatus = order.status.name,
            driverLatitude = latest?.latitude,
            driverLongitude = latest?.longitude,
            events = events.map { TrackingResponse.EventDto.from(it) }
        )
    }

    fun getEvents(orderId: Long): List<TrackingResponse.EventDto> {
        val user = userService.getCurrentUser()
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Pedido $orderId no encontrado") }
        if (order.userId != user.id && order.driverId != user.id) {
            throw AccessDeniedException("No tienes acceso a este pedido")
        }
        return orderEventRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
            .map { TrackingResponse.EventDto.from(it) }
    }

    @Transactional
    fun recordStatusEvent(orderId: Long, status: String, note: String? = null) {
        orderEventRepository.save(
            OrderEvent(orderId = orderId, status = status, note = note)
        )
    }

    @Transactional
    fun updateLocation(orderId: Long, request: UpdateLocationRequest) {
        val user = userService.getCurrentUser()
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Pedido $orderId no encontrado") }
        if (order.driverId != user.id) throw AccessDeniedException("Solo el repartidor asignado puede actualizar la ubicación")

        orderEventRepository.save(
            OrderEvent(
                orderId = orderId,
                status = order.status.name,
                latitude = request.latitude,
                longitude = request.longitude,
                note = request.note
            )
        )
    }
}
