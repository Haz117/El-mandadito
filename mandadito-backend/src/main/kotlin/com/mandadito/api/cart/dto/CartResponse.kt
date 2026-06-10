package com.mandadito.api.cart.dto

import com.mandadito.api.cart.Cart
import com.mandadito.api.menu.MenuItem

data class CartResponse(
    val restaurantId: Long?,
    val items: List<CartItemDetail>,
    val total: Double
) {
    data class CartItemDetail(
        val id: Long,
        val menuItemId: Long,
        val name: String,
        val price: Double,
        val quantity: Int,
        val subtotal: Double,
        val notes: String?
    )

    companion object {
        fun from(cart: Cart, menuItems: Map<Long, MenuItem>): CartResponse {
            val details = cart.items.mapNotNull { cartItem ->
                menuItems[cartItem.menuItemId]?.let { menuItem ->
                    CartItemDetail(
                        id = cartItem.id,
                        menuItemId = menuItem.id,
                        name = menuItem.name,
                        price = menuItem.price,
                        quantity = cartItem.quantity,
                        subtotal = menuItem.price * cartItem.quantity,
                        notes = cartItem.notes
                    )
                }
            }
            return CartResponse(
                restaurantId = cart.restaurantId,
                items = details,
                total = details.sumOf { it.subtotal }
            )
        }
    }
}
