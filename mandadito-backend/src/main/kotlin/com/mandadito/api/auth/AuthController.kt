package com.mandadito.api.auth

import com.mandadito.api.auth.dto.AuthResponse
import com.mandadito.api.auth.dto.LoginRequest
import com.mandadito.api.auth.dto.RegisterRequest
import com.mandadito.api.common.ApiResponse
import com.mandadito.api.user.UserService
import com.mandadito.api.user.dto.UserProfileResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registro, login y sesión")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService
) {

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(response, "Usuario registrado exitosamente"))
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse.ok(response, "Login exitoso"))
    }

    @GetMapping("/me")
    @Operation(summary = "Info del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    fun me(): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val user = userService.getCurrentUser()
        return ResponseEntity.ok(ApiResponse.ok(UserProfileResponse.from(user)))
    }
}
