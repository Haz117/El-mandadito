package com.mandadito.api.tracking.dto

import com.mandadito.api.tracking.OrderEvent
import java.time.LocalDateTime

data class TrackingResponse(
    val orderId: Long,
    val currentStatus: String,
    val driverLatitude: Double?,
    val driverLongitude: Double?,
    val events: List<EventDto>
) {
    data class EventDto(
        val id: Long,
        val status: String,
        val latitude: Double?,
        val longitude: Double?,
        val note: String?,
        val timestamp: LocalDateTime
    ) {
        companion object {
            fun from(e: OrderEvent) = EventDto(e.id, e.status, e.latitude, e.longitude, e.note, e.createdAt)
        }
    }
}
