package com.mandadito.api.admin

import com.mandadito.api.business.Business
import com.mandadito.api.business.BusinessRepository
import com.mandadito.api.business.BusinessStatus
import com.mandadito.api.business.dto.BusinessResponse
import com.mandadito.api.order.OrderRepository
import com.mandadito.api.order.dto.OrderResponse
import com.mandadito.api.restaurant.RestaurantRepository
import com.mandadito.api.restaurant.dto.RestaurantResponse
import com.mandadito.api.user.UserRepository
import com.mandadito.api.user.dto.UserProfileResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminService(
    private val businessRepository: BusinessRepository,
    private val orderRepository: OrderRepository,
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository
) {

    fun getPendingBusinesses(): List<BusinessResponse> =
        businessRepository.findByStatus(BusinessStatus.PENDING).map { BusinessResponse.from(it) }

    @Transactional
    fun approveBusiness(id: Long): BusinessResponse =
        updateBusinessStatus(id, BusinessStatus.APPROVED)

    @Transactional
    fun rejectBusiness(id: Long): BusinessResponse =
        updateBusinessStatus(id, BusinessStatus.REJECTED)

    @Transactional
    fun suspendBusiness(id: Long): BusinessResponse =
        updateBusinessStatus(id, BusinessStatus.SUSPENDED)

    fun getAllOrders(page: Int, size: Int): List<OrderResponse> =
        orderRepository.findAll().takeLast(size).map { OrderResponse.from(it) }

    fun getAllUsers(): List<UserProfileResponse> =
        userRepository.findAll().map { UserProfileResponse.from(it) }

    fun getAllRestaurants(): List<RestaurantResponse> =
        restaurantRepository.findAll().map { RestaurantResponse.from(it) }

    private fun updateBusinessStatus(id: Long, status: BusinessStatus): BusinessResponse {
        val business = businessRepository.findById(id)
            .orElseThrow { NoSuchElementException("Negocio $id no encontrado") }
        business.status = status
        business.updatedAt = LocalDateTime.now()
        return BusinessResponse.from(businessRepository.save(business))
    }
}
