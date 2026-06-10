package com.elmandadito.app.data

data class Restaurant(
    val id: Int,
    val name: String,
    val category: String,
    val emoji: String,
    val rating: Double,
    val deliveryTime: String,
    val deliveryFee: Int,
    val minimumOrder: Int,
    val tags: List<String>,
    val promo: String? = null,
    val isOpen: Boolean = true,
    val imageUri: String = "",
    val isNew: Boolean = false,
    val menu: List<MenuCategory>
)

data class MenuCategory(
    val name: String,
    val items: List<MenuItem>
)

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val emoji: String,
    val isPopular: Boolean = false
)

data class CartItem(
    val menuItem: MenuItem,
    val restaurantName: String,
    val restaurantCategory: String,
    var quantity: Int = 1
) {
    val totalPrice: Int get() = menuItem.price * quantity
}

data class FeaturedDeal(
    val title: String,
    val subtitle: String,
    val badge: String,
    val promoCode: String?,
    val bgType: Int   // 0=red, 1=gold, 2=dark
)
