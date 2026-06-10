package com.mandadito.api.review

import com.mandadito.api.order.OrderRepository
import com.mandadito.api.order.OrderStatus
import com.mandadito.api.restaurant.RestaurantRepository
import com.mandadito.api.review.dto.CreateReviewRequest
import com.mandadito.api.review.dto.ReviewResponse
import com.mandadito.api.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val orderRepository: OrderRepository,
    private val restaurantRepository: RestaurantRepository,
    private val userService: UserService
) {

    @Transactional
    fun create(request: CreateReviewRequest): ReviewResponse {
        val user = userService.getCurrentUser()

        val order = orderRepository.findById(request.orderId)
            .orElseThrow { NoSuchElementException("Pedido ${request.orderId} no encontrado") }

        if (order.userId != user.id) {
            throw IllegalArgumentException("No puedes calificar un pedido que no es tuyo")
        }
        if (order.status != OrderStatus.DELIVERED) {
            throw IllegalArgumentException("Solo puedes calificar pedidos entregados")
        }
        if (reviewRepository.existsByOrderIdAndUserId(request.orderId, user.id)) {
            throw IllegalArgumentException("Ya calificaste este pedido")
        }

        val review = Review(
            orderId = order.id,
            userId = user.id,
            restaurantId = order.restaurantId,
            driverId = order.driverId,
            restaurantRating = request.restaurantRating,
            driverRating = request.driverRating,
            comment = request.comment
        )
        val saved = reviewRepository.save(review)

        updateRestaurantRating(order.restaurantId)

        return ReviewResponse.from(saved)
    }

    fun getRestaurantReviews(restaurantId: Long): List<ReviewResponse> =
        reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
            .map { ReviewResponse.from(it) }

    fun getDriverReviews(driverId: Long): List<ReviewResponse> =
        reviewRepository.findByDriverIdOrderByCreatedAtDesc(driverId)
            .map { ReviewResponse.from(it) }

    private fun updateRestaurantRating(restaurantId: Long) {
        val restaurant = restaurantRepository.findById(restaurantId).orElse(null) ?: return
        val avg = reviewRepository.getAverageRating(restaurantId) ?: return
        val count = reviewRepository.countByRestaurantId(restaurantId)
        restaurant.rating = avg
        restaurant.totalRatings = count.toInt()
        restaurant.updatedAt = LocalDateTime.now()
        restaurantRepository.save(restaurant)
    }
}
