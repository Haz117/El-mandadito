package com.elmandadito.app.data

import android.content.Context
import android.content.SharedPreferences

object FavoritesManager {

    private const val PREFS_NAME = "favorites_prefs"
    private const val KEY_FAVORITES = "favorite_restaurant_ids"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isFavorite(restaurantId: Int): Boolean {
        return getFavoriteIds().contains(restaurantId.toString())
    }

    fun toggleFavorite(restaurantId: Int): Boolean {
        val ids = getFavoriteIds().toMutableSet()
        val key = restaurantId.toString()
        val nowFavorite = if (ids.contains(key)) {
            ids.remove(key)
            false
        } else {
            ids.add(key)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, ids).apply()
        return nowFavorite
    }

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun getFavoriteRestaurants(): List<Restaurant> {
        val ids = getFavoriteIds()
        return SampleData.restaurants.filter { it.id.toString() in ids }
    }
}
