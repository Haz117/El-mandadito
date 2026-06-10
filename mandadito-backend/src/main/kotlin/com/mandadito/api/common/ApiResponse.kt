package com.mandadito.api.common

import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> ok(data: T, message: String = "OK"): ApiResponse<T> =
            ApiResponse(success = true, message = message, data = data)

        fun <T> ok(message: String = "OK"): ApiResponse<T?> =
            ApiResponse(success = true, message = message, data = null)

        fun <T> error(message: String): ApiResponse<T?> =
            ApiResponse(success = false, message = message, data = null)
    }
}
