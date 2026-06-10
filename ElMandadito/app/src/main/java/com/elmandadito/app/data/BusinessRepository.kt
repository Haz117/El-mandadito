package com.elmandadito.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class BusinessData(
    val id: String,
    val ownerEmail: String,
    val name: String,
    val category: String,
    val emoji: String,
    val tags: List<String>,
    val deliveryTime: String,
    val deliveryFee: Int,
    val minimumOrder: Int,
    val phone: String,
    val promo: String?,
    val isOpen: Boolean,
    val imageUri: String,
    val menuItems: List<MenuItem>,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toRestaurant() = Restaurant(
        id = kotlin.math.abs(id.hashCode()),
        name = name,
        category = category,
        emoji = emoji,
        tags = tags,
        rating = 5.0,
        deliveryTime = deliveryTime,
        deliveryFee = deliveryFee,
        minimumOrder = minimumOrder,
        isOpen = isOpen,
        isNew = (System.currentTimeMillis() - createdAt) < 7L * 24 * 60 * 60 * 1000,
        promo = promo?.ifBlank { null },
        imageUri = imageUri,
        menu = listOf(MenuCategory("Menú", menuItems))
    )
}

object BusinessRepository {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("business_prefs", Context.MODE_PRIVATE)
    }

    fun save(business: BusinessData) {
        val all = getAll().toMutableList()
        val idx = all.indexOfFirst { it.id == business.id }
        if (idx >= 0) all[idx] = business else all.add(business)
        saveAll(all)
    }

    fun getAll(): List<BusinessData> {
        if (!::prefs.isInitialized) return emptyList()
        val json = prefs.getString("businesses", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { emptyList() }
    }

    fun getByOwner(email: String): List<BusinessData> = getAll().filter { it.ownerEmail == email }

    fun delete(id: String) {
        val all = getAll().toMutableList()
        all.removeAll { it.id == id }
        saveAll(all)
    }

    fun newId(): String = UUID.randomUUID().toString()

    private fun saveAll(list: List<BusinessData>) {
        val arr = JSONArray().apply { list.forEach { put(toJson(it)) } }
        prefs.edit().putString("businesses", arr.toString()).apply()
    }

    private fun toJson(b: BusinessData) = JSONObject().apply {
        put("id", b.id); put("ownerEmail", b.ownerEmail)
        put("name", b.name); put("category", b.category)
        put("emoji", b.emoji); put("tags", b.tags.joinToString(","))
        put("deliveryTime", b.deliveryTime); put("deliveryFee", b.deliveryFee)
        put("minimumOrder", b.minimumOrder); put("phone", b.phone)
        put("promo", b.promo ?: ""); put("isOpen", b.isOpen); put("imageUri", b.imageUri)
        put("createdAt", b.createdAt)
        put("menuItems", JSONArray().apply {
            b.menuItems.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id); put("name", item.name)
                    put("description", item.description); put("price", item.price)
                    put("emoji", item.emoji); put("isPopular", item.isPopular)
                })
            }
        })
    }

    private fun fromJson(obj: JSONObject): BusinessData {
        val menuArr = obj.optJSONArray("menuItems") ?: JSONArray()
        return BusinessData(
            id = obj.getString("id"),
            ownerEmail = obj.optString("ownerEmail", ""),
            name = obj.getString("name"),
            category = obj.optString("category", "mexican"),
            emoji = obj.optString("emoji", "🍽️"),
            tags = obj.optString("tags", "").split(",").filter { it.isNotBlank() },
            deliveryTime = obj.optString("deliveryTime", "30-45 min"),
            deliveryFee = obj.optInt("deliveryFee", 0),
            minimumOrder = obj.optInt("minimumOrder", 0),
            phone = obj.optString("phone", ""),
            promo = obj.optString("promo", "").ifBlank { null },
            isOpen = obj.optBoolean("isOpen", true),
            imageUri = obj.optString("imageUri", ""),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            menuItems = (0 until menuArr.length()).map { i ->
                val item = menuArr.getJSONObject(i)
                MenuItem(
                    id = item.optInt("id", i + 10000),
                    name = item.getString("name"),
                    description = item.optString("description", ""),
                    price = item.optInt("price", 0),
                    emoji = item.optString("emoji", "🍽️"),
                    isPopular = item.optBoolean("isPopular", false)
                )
            }
        )
    }
}
