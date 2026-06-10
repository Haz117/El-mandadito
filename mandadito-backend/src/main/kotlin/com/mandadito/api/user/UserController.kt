package com.mandadito.api.user

import com.mandadito.api.common.ApiResponse
import com.mandadito.api.user.dto.UpdateProfileRequest
import com.mandadito.api.user.dto.UserProfileResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestión del perfil de usuario")
@SecurityRequirement(name = "bearerAuth")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    fun getProfile(): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val user = userService.getCurrentUser()
        return ResponseEntity.ok(ApiResponse.ok(UserProfileResponse.from(user)))
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil del usuario autenticado")
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val user = userService.getCurrentUser()
        val updated = userService.updateProfile(user, request.name, request.phone)
        return ResponseEntity.ok(ApiResponse.ok(UserProfileResponse.from(updated), "Perfil actualizado"))
    }
}
