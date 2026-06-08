package com.elmandadito.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import org.json.JSONObject

object CartRepository {
    val items = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val promoDiscount = MutableLiveData(0)

    private lateinit var prefs: SharedPreferences

    val currentRestaurantName: String?
        get() = items.value?.firstOrNull()?.restaurantName

    fun init(context: Context) {
        prefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
        loadFromPrefs()
    }

    fun addItem(menuItem: MenuItem, restaurantName: String, restaurantCategory: String) {
        val list = items.value ?: mutableListOf()
        val existing = list.find { it.menuItem.id == menuItem.id }
        if (existing != null) existing.quantity++
        else list.add(CartItem(menuItem, restaurantName, restaurantCategory))
        items.value = list
        saveToPrefs()
    }

    fun increaseQty(itemId: Int) {
        val list = items.value ?: return
        list.find { it.menuItem.id == itemId }?.quantity++
        items.value = list
        saveToPrefs()
    }

    fun decreaseQty(itemId: Int) {
        val list = items.value ?: return
        val item = list.find { it.menuItem.id == itemId } ?: return
        if (item.quantity > 1) item.quantity-- else list.remove(item)
        items.value = list
        saveToPrefs()
    }

    fun applyPromo(code: String): Boolean {
        val pct = when (code.uppercase().trim()) {
            "MANDADITO20" -> 20
            "BIENVENIDO"  -> 15
            "PROMO10"     -> 10
            else          -> return false
        }
        promoDiscount.value = pct
        saveToPrefs()
        return true
    }

    fun removeItem(item: CartItem) {
        val list = items.value ?: return
        list.remove(item)
        items.value = list
        saveToPrefs()
    }

    fun addBack(item: CartItem) {
        val list = items.value ?: mutableListOf()
        list.add(item)
        items.value = list
        saveToPrefs()
    }

    fun clearCart() {
        items.value = mutableListOf()
        promoDiscount.value = 0
        saveToPrefs()
    }

    fun subtotal(): Int = items.value?.sumOf { it.totalPrice } ?: 0
    fun deliveryFee(): Int = if (subtotal() >= 200) 0 else 35
    fun discountAmount(): Int = subtotal() * (promoDiscount.value ?: 0) / 100
    fun total(): Int = subtotal() + deliveryFee() - discountAmount()
    fun itemCount(): Int = items.value?.sumOf { it.quantity } ?: 0
    fun remainingForFreeDelivery(): Int = maxOf(0, 200 - subtotal())

    private fun saveToPrefs() {
        val arr = JSONArray()
        items.value?.forEach { ci ->
            arr.put(JSONObject().apply {
                put("item_id", ci.menuItem.id)
                put("item_name", ci.menuItem.name)
                put("item_desc", ci.menuItem.description)
                put("item_price", ci.menuItem.price)
                put("item_emoji", ci.menuItem.emoji)
                put("item_popular", ci.menuItem.isPopular)
                put("restaurant_name", ci.restaurantName)
                put("restaurant_category", ci.restaurantCategory)
                put("quantity", ci.quantity)
            })
        }
        prefs.edit()
            .putString("cart_json", arr.toString())
            .putInt("promo_pct", promoDiscount.value ?: 0)
            .apply()
    }

    private fun loadFromPrefs() {
        val json = prefs.getString("cart_json", "[]") ?: "[]"
        val pct = prefs.getInt("promo_pct", 0)
        try {
            val arr = JSONArray(json)
            val list = mutableListOf<CartItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val menuItem = MenuItem(
                    id = obj.getInt("item_id"),
                    name = obj.getString("item_name"),
                    description = obj.getString("item_desc"),
                    price = obj.getInt("item_price"),
                    emoji = obj.getString("item_emoji"),
                    isPopular = obj.getBoolean("item_popular")
                )
                list.add(CartItem(
                    menuItem = menuItem,
                    restaurantName = obj.getString("restaurant_name"),
                    restaurantCategory = obj.getString("restaurant_category"),
                    quantity = obj.getInt("quantity")
                ))
            }
            items.value = list
            promoDiscount.value = pct
        } catch (_: Exception) {
            items.value = mutableListOf()
        }
    }
}
