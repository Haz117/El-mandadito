package com.mandadito.api.address

import com.mandadito.api.address.dto.AddressResponse
import com.mandadito.api.address.dto.CreateAddressRequest
import com.mandadito.api.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users/me/addresses")
@Tag(name = "Addresses", description = "Direcciones de entrega del usuario")
@SecurityRequirement(name = "bearerAuth")
class AddressController(private val addressService: AddressService) {

    @GetMapping
    @Operation(summary = "Listar mis direcciones")
    fun getMyAddresses(): ResponseEntity<ApiResponse<List<AddressResponse>>> {
        val response = addressService.getMyAddresses()
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PostMapping
    @Operation(summary = "Agregar dirección")
    fun create(
        @Valid @RequestBody request: CreateAddressRequest
    ): ResponseEntity<ApiResponse<AddressResponse>> {
        val response = addressService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Dirección guardada"))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar dirección")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateAddressRequest
    ): ResponseEntity<ApiResponse<AddressResponse>> {
        val response = addressService.update(id, request)
        return ResponseEntity.ok(ApiResponse.ok(response, "Dirección actualizada"))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar dirección")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing?>> {
        addressService.delete(id)
        return ResponseEntity.ok(ApiResponse.ok("Dirección eliminada"))
    }
}
