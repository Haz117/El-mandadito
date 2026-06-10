package com.mandadito.api.business

import com.mandadito.api.business.dto.BusinessResponse
import com.mandadito.api.business.dto.CreateBusinessRequest
import com.mandadito.api.business.dto.UpdateBusinessRequest
import com.mandadito.api.user.UserService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BusinessService(
    private val businessRepository: BusinessRepository,
    private val userService: UserService
) {

    @Transactional
    fun create(request: CreateBusinessRequest): BusinessResponse {
        val user = userService.getCurrentUser()
        val business = Business(
            ownerId = user.id,
            name = request.name,
            description = request.description,
            phone = request.phone,
            email = request.email,
            address = request.address,
            city = request.city
        )
        return BusinessResponse.from(businessRepository.save(business))
    }

    fun getMyBusinesses(): List<BusinessResponse> {
        val user = userService.getCurrentUser()
        return businessRepository.findByOwnerId(user.id).map { BusinessResponse.from(it) }
    }

    fun getById(id: Long): BusinessResponse {
        val user = userService.getCurrentUser()
        val business = findAndValidateOwner(id, user.id)
        return BusinessResponse.from(business)
    }

    @Transactional
    fun update(id: Long, request: UpdateBusinessRequest): BusinessResponse {
        val user = userService.getCurrentUser()
        val business = findAndValidateOwner(id, user.id)
        business.name = request.name
        business.description = request.description
        business.phone = request.phone
        business.email = request.email
        business.address = request.address
        business.city = request.city
        request.logoUrl?.let { business.logoUrl = it }
        business.updatedAt = LocalDateTime.now()
        return BusinessResponse.from(businessRepository.save(business))
    }

    fun findByIdInternal(id: Long): Business =
        businessRepository.findById(id)
            .orElseThrow { NoSuchElementException("Negocio $id no encontrado") }

    private fun findAndValidateOwner(id: Long, ownerId: Long): Business {
        val business = businessRepository.findById(id)
            .orElseThrow { NoSuchElementException("Negocio $id no encontrado") }
        if (business.ownerId != ownerId) {
            throw AccessDeniedException("No tienes permiso para modificar este negocio")
        }
        return business
    }
}
