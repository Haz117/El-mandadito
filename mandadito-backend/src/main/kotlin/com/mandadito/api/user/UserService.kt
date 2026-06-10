package com.mandadito.api.user

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(private val userRepository: UserRepository) {

    fun getCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication.name
        return userRepository.findByEmail(email)
            .orElseThrow { NoSuchElementException("Usuario no encontrado") }
    }

    fun findById(id: Long): User =
        userRepository.findById(id)
            .orElseThrow { NoSuchElementException("Usuario $id no encontrado") }

    @Transactional
    fun updateProfile(user: User, name: String, phone: String?): User {
        user.name = name
        user.phone = phone
        user.updatedAt = LocalDateTime.now()
        return userRepository.save(user)
    }
}
