package com.mandadito.api.address.dto

import com.mandadito.api.address.Address

data class AddressResponse(
    val id: Long,
    val street: String,
    val number: String?,
    val neighborhood: String?,
    val city: String,
    val state: String?,
    val zipCode: String?,
    val reference: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isDefault: Boolean
) {
    companion object {
        fun from(a: Address) = AddressResponse(
            id = a.id,
            street = a.street,
            number = a.number,
            neighborhood = a.neighborhood,
            city = a.city,
            state = a.state,
            zipCode = a.zipCode,
            reference = a.reference,
            latitude = a.latitude,
            longitude = a.longitude,
            isDefault = a.isDefault
        )
    }
}
