package com.elmandadito.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OrderRecord(
    val restaurantName: String,
    val total: Int,
    val itemCount: Int,
    val date: String,
    val paymentMethod: String,
    val ratingStars: Int = 0
)

object OrderHistoryManager {

    private const val PREFS_NAME = "order_history_prefs"
    private const val KEY_ORDERS = "orders_json"
    private const val MAX_ORDERS = 10

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveOrder(restaurantName: String, total: Int, itemCount: Int, paymentMethod: String) {
        val dateStr = SimpleDateFormat("dd MMM", Locale("es", "MX")).format(Date())
        val newOrder = OrderRecord(restaurantName, total, itemCount, dateStr, paymentMethod)
        val orders = getOrders().toMutableList()
        orders.add(0, newOrder)
        if (orders.size > MAX_ORDERS) orders.removeAt(orders.lastIndex)
        saveOrders(orders)
    }

    fun updateLatestOrderRating(stars: Int) {
        val orders = getOrders().toMutableList()
        if (orders.isEmpty()) return
        orders[0] = orders[0].copy(ratingStars = stars)
        saveOrders(orders)
    }

    fun getOrders(): List<OrderRecord> {
        val json = prefs.getString(KEY_ORDERS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                OrderRecord(
                    restaurantName = obj.getString("restaurant"),
                    total = obj.getInt("total"),
                    itemCount = obj.getInt("items"),
                    date = obj.getString("date"),
                    paymentMethod = obj.optString("payment", "Efectivo"),
                    ratingStars = obj.optInt("rating", 0)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getTotalOrderCount(): Int = getOrders().size

    fun getTotalSpent(): Int = getOrders().sumOf { it.total }

    fun getLoyaltyPoints(): Int = getOrders().sumOf { it.itemCount * 10 + it.total / 10 }

    fun getLoyaltyTier(points: Int): Triple<String, Int, Int> = when {
        points >= 600 -> Triple("Platino", 600, 1000)
        points >= 300 -> Triple("Oro", 300, 600)
        points >= 100 -> Triple("Plata", 100, 300)
        else          -> Triple("Bronce", 0, 100)
    }

    private fun saveOrders(orders: List<OrderRecord>) {
        val arr = JSONArray()
        orders.forEach { o ->
            arr.put(JSONObject().apply {
                put("restaurant", o.restaurantName)
                put("total", o.total)
                put("items", o.itemCount)
                put("date", o.date)
                put("payment", o.paymentMethod)
                put("rating", o.ratingStars)
            })
        }
        prefs.edit().putString(KEY_ORDERS, arr.toString()).apply()
    }
}
