package com.mandadito.api.order

enum class OrderStatus {
    CREATED,
    ACCEPTED_BY_RESTAURANT,
    PREPARING,
    READY_FOR_PICKUP,
    DRIVER_ASSIGNED,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED,
    REJECTED
}
