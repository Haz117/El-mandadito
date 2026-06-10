package com.mandadito.api.order

import com.mandadito.api.address.AddressService
import com.mandadito.api.business.BusinessRepository
import com.mandadito.api.cart.CartRepository
import com.mandadito.api.cart.CartService
import com.mandadito.api.menu.MenuItemRepository
import com.mandadito.api.order.dto.CreateOrderRequest
import com.mandadito.api.order.dto.OrderResponse
import com.mandadito.api.restaurant.RestaurantRepository
import com.mandadito.api.user.UserService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val menuItemRepository: MenuItemRepository,
    private val restaurantRepository: RestaurantRepository,
    private val businessRepository: BusinessRepository,
    private val addressService: AddressService,
    private val cartService: CartService,
    private val userService: UserService
) {

    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        val user = userService.getCurrentUser()

        val address = addressService.findByIdInternal(request.addressId)
        if (address.userId != user.id) throw AccessDeniedException("La dirección no pertenece a tu cuenta")

        val cart = cartRepository.findByUserId(user.id)
            .orElseThrow { IllegalArgumentException("Tu carrito está vacío") }
        if (cart.items.isEmpty() || cart.restaurantId == null) throw IllegalArgumentException("Tu carrito está vacío")

        val restaurant = restaurantRepository.findById(cart.restaurantId!!)
            .orElseThrow { NoSuchElementException("Restaurante no encontrado") }
        if (!restaurant.isOpen) throw IllegalArgumentException("El restaurante está cerrado en este momento")

        val menuItems = menuItemRepository.findAllById(cart.items.map { it.menuItemId }).associateBy { it.id }

        // Pre-calcular items para saber el subtotal antes de construir Order
        data class ItemData(val menuItemId: Long, val name: String, val price: Double, val qty: Int, val notes: String?, val subtotal: Double)
        val itemsData = cart.items.map { cartItem ->
            val mi = menuItems[cartItem.menuItemId] ?: throw IllegalArgumentException("Producto no encontrado en el menú")
            if (!mi.available) throw IllegalArgumentException("El producto '${mi.name}' ya no está disponible")
            ItemData(mi.id, mi.name, mi.price, cartItem.quantity, cartItem.notes, mi.price * cartItem.quantity)
        }

        val subtotal = itemsData.sumOf { it.subtotal }
        val total = subtotal + restaurant.deliveryFee

        val order = Order(
            userId = user.id,
            restaurantId = restaurant.id,
            addressId = address.id,
            subtotal = subtotal,
            deliveryFee = restaurant.deliveryFee,
            total = total,
            paymentMethod = request.paymentMethod,
            notes = request.notes
        )

        itemsData.forEach { item ->
            order.items.add(OrderItem(
                order = order,
                menuItemId = item.menuItemId,
                nameSnapshot = item.name,
                priceSnapshot = item.price,
                quantity = item.qty,
                subtotal = item.subtotal,
                notes = item.notes
            ))
        }

        val saved = orderRepository.save(order)
        cartService.clearCart(user.id)
        return OrderResponse.from(saved)
    }

    fun getMyOrders(): List<OrderResponse> {
        val user = userService.getCurrentUser()
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.id).map { OrderResponse.from(it) }
    }

    fun getOrderById(id: Long): OrderResponse {
        val user = userService.getCurrentUser()
        val order = orderRepository.findById(id)
            .orElseThrow { NoSuchElementException("Pedido $id no encontrado") }
        if (order.userId != user.id) validateRestaurantOwner(order.restaurantId, user.id)
        return OrderResponse.from(order)
    }

    fun getRestaurantOrders(restaurantId: Long): List<OrderResponse> {
        val user = userService.getCurrentUser()
        validateRestaurantOwner(restaurantId, user.id)
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).map { OrderResponse.from(it) }
    }

    @Transactional
    fun updateStatus(orderId: Long, newStatus: OrderStatus): OrderResponse {
        val user = userService.getCurrentUser()
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Pedido $orderId no encontrado") }

        when (newStatus) {
            OrderStatus.ACCEPTED_BY_RESTAURANT,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.REJECTED -> validateRestaurantOwner(order.restaurantId, user.id)
            OrderStatus.ON_THE_WAY,
            OrderStatus.DELIVERED -> {
                if (order.driverId != user.id) throw AccessDeniedException("Solo el repartidor asignado puede actualizar este estado")
            }
            else -> {}
        }

        order.status = newStatus
        order.updatedAt = LocalDateTime.now()
        if (newStatus == OrderStatus.DELIVERED) order.deliveredAt = LocalDateTime.now()
        if (newStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED)) order.cancelledAt = LocalDateTime.now()

        return OrderResponse.from(orderRepository.save(order))
    }

    @Transactional
    fun cancelOrder(orderId: Long): OrderResponse {
        val user = userService.getCurrentUser()
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Pedido $orderId no encontrado") }

        if (order.userId != user.id) throw AccessDeniedException("No puedes cancelar este pedido")

        if (order.status !in setOf(OrderStatus.CREATED, OrderStatus.ACCEPTED_BY_RESTAURANT)) {
            throw IllegalArgumentException("El pedido no puede cancelarse en su estado actual: ${order.status}")
        }

        order.status = OrderStatus.CANCELLED
        order.cancelledAt = LocalDateTime.now()
        order.updatedAt = LocalDateTime.now()
        return OrderResponse.from(orderRepository.save(order))
    }

    @Transactional
    fun assignDriver(orderId: Long, driverId: Long): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Pedido $orderId no encontrado") }
        order.driverId = driverId
        order.status = OrderStatus.DRIVER_ASSIGNED
        order.updatedAt = LocalDateTime.now()
        return OrderResponse.from(orderRepository.save(order))
    }

    private fun validateRestaurantOwner(restaurantId: Long, userId: Long) {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { NoSuchElementException("Restaurante no encontrado") }
        val business = businessRepository.findById(restaurant.businessId)
            .orElseThrow { NoSuchElementException("Negocio no encontrado") }
        if (business.ownerId != userId) throw AccessDeniedException("No tienes permiso para gestionar pedidos de este restaurante")
    }
}
