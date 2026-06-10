package com.mandadito.api.promo

import com.mandadito.api.promo.dto.CreatePromotionRequest
import com.mandadito.api.promo.dto.PromotionResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class PromoService(private val promotionRepository: PromotionRepository) {

    fun getActivePromotions(): List<PromotionResponse> =
        promotionRepository.findActivePromotions(LocalDate.now()).map { PromotionResponse.from(it) }

    fun getById(id: Long): PromotionResponse =
        PromotionResponse.from(
            promotionRepository.findById(id).orElseThrow { NoSuchElementException("Promoción $id no encontrada") }
        )

    @Transactional
    fun create(request: CreatePromotionRequest): PromotionResponse {
        val promo = Promotion(
            title = request.title,
            description = request.description,
            imageUrl = request.imageUrl,
            discountType = request.discountType,
            discountValue = request.discountValue,
            startDate = request.startDate,
            endDate = request.endDate,
            active = request.active
        )
        return PromotionResponse.from(promotionRepository.save(promo))
    }

    @Transactional
    fun update(id: Long, request: CreatePromotionRequest): PromotionResponse {
        val promo = promotionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Promoción $id no encontrada") }
        promo.title = request.title
        promo.description = request.description
        promo.imageUrl = request.imageUrl
        promo.discountType = request.discountType
        promo.discountValue = request.discountValue
        promo.startDate = request.startDate
        promo.endDate = request.endDate
        promo.active = request.active
        promo.updatedAt = LocalDateTime.now()
        return PromotionResponse.from(promotionRepository.save(promo))
    }

    @Transactional
    fun delete(id: Long) {
        val promo = promotionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Promoción $id no encontrada") }
        promotionRepository.delete(promo)
    }

    @Transactional
    fun toggleActive(id: Long): PromotionResponse {
        val promo = promotionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Promoción $id no encontrada") }
        promo.active = !promo.active
        promo.updatedAt = LocalDateTime.now()
        return PromotionResponse.from(promotionRepository.save(promo))
    }
}
