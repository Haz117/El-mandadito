package com.mandadito.api.address

import com.mandadito.api.address.dto.AddressResponse
import com.mandadito.api.address.dto.CreateAddressRequest
import com.mandadito.api.user.UserService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AddressService(
    private val addressRepository: AddressRepository,
    private val userService: UserService
) {

    fun getMyAddresses(): List<AddressResponse> {
        val user = userService.getCurrentUser()
        return addressRepository.findByUserId(user.id).map { AddressResponse.from(it) }
    }

    @Transactional
    fun create(request: CreateAddressRequest): AddressResponse {
        val user = userService.getCurrentUser()

        if (request.isDefault) {
            clearDefaultAddress(user.id)
        }

        val address = Address(
            userId = user.id,
            street = request.street,
            number = request.number,
            neighborhood = request.neighborhood,
            city = request.city,
            state = request.state,
            zipCode = request.zipCode,
            reference = request.reference,
            latitude = request.latitude,
            longitude = request.longitude,
            isDefault = request.isDefault
        )
        return AddressResponse.from(addressRepository.save(address))
    }

    @Transactional
    fun update(id: Long, request: CreateAddressRequest): AddressResponse {
        val user = userService.getCurrentUser()
        val address = findAndValidateOwner(id, user.id)

        if (request.isDefault) {
            clearDefaultAddress(user.id)
        }

        address.street = request.street
        address.number = request.number
        address.neighborhood = request.neighborhood
        address.city = request.city
        address.state = request.state
        address.zipCode = request.zipCode
        address.reference = request.reference
        address.latitude = request.latitude
        address.longitude = request.longitude
        address.isDefault = request.isDefault
        return AddressResponse.from(addressRepository.save(address))
    }

    @Transactional
    fun delete(id: Long) {
        val user = userService.getCurrentUser()
        val address = findAndValidateOwner(id, user.id)
        addressRepository.delete(address)
    }

    fun findByIdInternal(id: Long): Address =
        addressRepository.findById(id)
            .orElseThrow { NoSuchElementException("Dirección $id no encontrada") }

    private fun clearDefaultAddress(userId: Long) {
        addressRepository.findByUserIdAndIsDefault(userId, true)?.let { existing ->
            existing.isDefault = false
            addressRepository.save(existing)
        }
    }

    private fun findAndValidateOwner(id: Long, userId: Long): Address {
        val address = addressRepository.findById(id)
            .orElseThrow { NoSuchElementException("Dirección $id no encontrada") }
        if (address.userId != userId) {
            throw AccessDeniedException("No tienes permiso para modificar esta dirección")
        }
        return address
    }
}
