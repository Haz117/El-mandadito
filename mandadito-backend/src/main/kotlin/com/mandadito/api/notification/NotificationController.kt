package com.mandadito.api.notification

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.notification.dto.RegisterTokenRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Notifications", description = "Tokens de dispositivo para notificaciones push")
@SecurityRequirement(name = "bearerAuth")
class NotificationController(private val notificationService: NotificationService) {

    @PostMapping("/register-token")
    @Operation(summary = "Registrar token FCM del dispositivo")
    fun registerToken(
        @Valid @RequestBody request: RegisterTokenRequest
    ): ResponseEntity<ApiResponse<Nothing?>> {
        notificationService.registerToken(request)
        return ResponseEntity.ok(ApiResponse.ok("Token registrado"))
    }

    @DeleteMapping("/{token}")
    @Operation(summary = "Desregistrar token FCM")
    fun unregisterToken(@PathVariable token: String): ResponseEntity<ApiResponse<Nothing?>> {
        notificationService.unregisterToken(token)
        return ResponseEntity.ok(ApiResponse.ok("Token eliminado"))
    }
}
