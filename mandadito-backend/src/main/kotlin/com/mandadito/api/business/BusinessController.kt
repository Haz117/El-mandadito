package com.mandadito.api.business

import com.mandadito.api.business.dto.BusinessResponse
import com.mandadito.api.business.dto.CreateBusinessRequest
import com.mandadito.api.business.dto.UpdateBusinessRequest
import com.mandadito.api.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Businesses", description = "Gestión de negocios propios")
@SecurityRequirement(name = "bearerAuth")
class BusinessController(private val businessService: BusinessService) {

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Registrar un nuevo negocio")
    fun create(
        @Valid @RequestBody request: CreateBusinessRequest
    ): ResponseEntity<ApiResponse<BusinessResponse>> {
        val response = businessService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(response, "Negocio registrado. Pendiente de aprobación."))
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Mis negocios registrados")
    fun getMyBusinesses(): ResponseEntity<ApiResponse<List<BusinessResponse>>> {
        val response = businessService.getMyBusinesses()
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Detalle de un negocio")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<BusinessResponse>> {
        val response = businessService.getById(id)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    @Operation(summary = "Actualizar negocio")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBusinessRequest
    ): ResponseEntity<ApiResponse<BusinessResponse>> {
        val response = businessService.update(id, request)
        return ResponseEntity.ok(ApiResponse.ok(response, "Negocio actualizado"))
    }
}
