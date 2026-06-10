package com.mandadito.api.loyalty

import com.mandadito.api.loyalty.dto.LoyaltyResponse
import com.mandadito.api.loyalty.dto.RedeemRequest
import com.mandadito.api.user.UserRepository
import com.mandadito.api.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LoyaltyService(
    private val loyaltyPointRepository: LoyaltyPointRepository,
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    fun getMyLoyalty(): LoyaltyResponse {
        val user = userService.getCurrentUser()
        val points = user.mandapoints
        val history = loyaltyPointRepository.findByUserIdOrderByCreatedAtDesc(user.id)
        return LoyaltyResponse(
            totalPoints = points,
            level = LoyaltyResponse.levelFor(points),
            history = history.map { LoyaltyResponse.LoyaltyHistoryDto.from(it) }
        )
    }

    @Transactional
    fun redeemPoints(request: RedeemRequest) {
        val user = userService.getCurrentUser()
        if (user.mandapoints < request.points) {
            throw IllegalArgumentException("Puntos insuficientes. Tienes ${user.mandapoints} Mandapoints.")
        }
        loyaltyPointRepository.save(
            LoyaltyPoint(
                userId = user.id,
                points = request.points,
                type = LoyaltyPointType.REDEEMED,
                description = "Canje de ${request.points} Mandapoints"
            )
        )
        user.mandapoints -= request.points
        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)
    }

    @Transactional
    fun earnPoints(userId: Long, orderId: Long, orderTotal: Double) {
        val pointsEarned = (orderTotal / 10).toInt().coerceAtLeast(1)
        val user = userRepository.findById(userId).orElse(null) ?: return
        loyaltyPointRepository.save(
            LoyaltyPoint(
                userId = userId,
                points = pointsEarned,
                type = LoyaltyPointType.EARNED,
                description = "Puntos por pedido #$orderId",
                orderId = orderId
            )
        )
        user.mandapoints += pointsEarned
        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)
    }
}
