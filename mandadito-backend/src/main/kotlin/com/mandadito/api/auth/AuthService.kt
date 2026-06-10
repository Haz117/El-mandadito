package com.mandadito.api.auth

import com.mandadito.api.auth.dto.AuthResponse
import com.mandadito.api.auth.dto.AuthUserDto
import com.mandadito.api.auth.dto.LoginRequest
import com.mandadito.api.auth.dto.RegisterRequest
import com.mandadito.api.security.JwtTokenProvider
import com.mandadito.api.user.Role
import com.mandadito.api.user.User
import com.mandadito.api.user.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val role = runCatching { Role.valueOf(request.role.uppercase()) }
            .getOrDefault(Role.USER)
            .let { if (it == Role.ADMIN) Role.USER else it }

        val user = User(
            name = request.name,
            email = request.email.lowercase().trim(),
            password = passwordEncoder.encode(request.password),
            phone = request.phone,
            role = role
        )

        val saved = userRepository.save(user)
        val token = jwtTokenProvider.generateToken(saved.email, saved.role.name)
        return AuthResponse(token = token, user = AuthUserDto.from(saved))
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email.lowercase().trim(), request.password)
        )

        val user = userRepository.findByEmail(request.email.lowercase().trim())
            .orElseThrow { NoSuchElementException("Usuario no encontrado") }

        val token = jwtTokenProvider.generateToken(user.email, user.role.name)
        return AuthResponse(token = token, user = AuthUserDto.from(user))
    }
}
