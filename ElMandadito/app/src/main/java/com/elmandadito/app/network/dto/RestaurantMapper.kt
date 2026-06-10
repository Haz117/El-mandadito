package com.elmandadito.app.network.dto

import com.elmandadito.app.data.MenuCategory
import com.elmandadito.app.data.MenuItem
import com.elmandadito.app.data.Restaurant

fun RestaurantResponse.toRestaurant(): Restaurant = Restaurant(
    id = (id % Int.MAX_VALUE).toInt(),
    networkId = id,
    name = name,
    category = category?.lowercase() ?: "other",
    emoji = categoryEmoji(category?.lowercase()),
    rating = rating,
    deliveryTime = "$deliveryTimeMin–$deliveryTimeMax min",
    deliveryFee = deliveryFee.toInt(),
    minimumOrder = 50,
    tags = categoryTags(category?.lowercase()),
    promo = null,
    isOpen = isOpen,
    imageUri = imageUrl ?: "",
    isNew = false,
    menu = emptyList()
)

fun MenuItemResponse.toMenuItem(): MenuItem = MenuItem(
    id = (id % Int.MAX_VALUE).toInt(),
    name = name,
    description = description ?: "",
    price = price.toInt(),
    emoji = categoryEmoji(category?.lowercase()),
    isPopular = false
)

fun List<MenuItemResponse>.toMenuCategories(): List<MenuCategory> =
    groupBy { it.category ?: "Menú" }
        .map { (cat, items) ->
            MenuCategory(
                name = cat,
                items = items.filter { it.available }.map { it.toMenuItem() }
            )
        }

private fun categoryEmoji(cat: String?) = when (cat) {
    "mexican"  -> "🌮"
    "burgers"  -> "🍔"
    "pizza"    -> "🍕"
    "sushi"    -> "🍱"
    "chicken"  -> "🍗"
    "desserts" -> "🍰"
    else       -> "🍽️"
}

private fun categoryTags(cat: String?) = when (cat) {
    "mexican"  -> listOf("tacos", "burritos", "comida mexicana")
    "burgers"  -> listOf("hamburguesas", "papas", "combos")
    "pizza"    -> listOf("pizza", "pasta", "italiana")
    "sushi"    -> listOf("sushi", "japonesa", "rolls")
    "chicken"  -> listOf("pollo", "alitas", "nuggets")
    "desserts" -> listOf("postres", "helados", "pasteles")
    else       -> emptyList()
}
