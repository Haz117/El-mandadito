package com.elmandadito.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

object UserPrefsManager {
    private const val PREFS = "user_prefs"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PASSWORD_HASH = "user_password_hash"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_STAR_RATING = "star_rating"
    private const val KEY_SANCTION_COUNT = "sanction_count"
    private const val KEY_SANCTION_HISTORY = "sanction_history"
    private const val KEY_IS_BLOCKED = "is_blocked"
    private const val KEY_CURP = "user_curp"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun getName(): String = prefs.getString(KEY_NAME, "Usuario") ?: "Usuario"
    fun setName(name: String) = prefs.edit().putString(KEY_NAME, name.trim().ifBlank { "Usuario" }).apply()

    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun setEmail(email: String) = prefs.edit().putString(KEY_EMAIL, email.trim()).apply()

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun setLoggedIn(value: Boolean) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    fun getPasswordHash(): String = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""
    fun setPasswordHash(hash: String) = prefs.edit().putString(KEY_PASSWORD_HASH, hash).apply()

    fun getStarRating(): Int = prefs.getInt(KEY_STAR_RATING, 5)
    fun setStarRating(stars: Int) = prefs.edit().putInt(KEY_STAR_RATING, stars.coerceIn(0, 5)).apply()

    fun getSanctionCount(): Int = prefs.getInt(KEY_SANCTION_COUNT, 0)
    fun incrementSanctions() = prefs.edit().putInt(KEY_SANCTION_COUNT, getSanctionCount() + 1).apply()

    fun isBlocked(): Boolean = prefs.getBoolean(KEY_IS_BLOCKED, false)
    fun setBlocked(value: Boolean) = prefs.edit().putBoolean(KEY_IS_BLOCKED, value).apply()

    fun getCurp(): String = prefs.getString(KEY_CURP, "") ?: ""
    fun setCurp(curp: String) = prefs.edit().putString(KEY_CURP, curp).apply()

    fun hasAccount(): Boolean = getEmail().isNotEmpty() && getPasswordHash().isNotEmpty()

    fun getSanctionHistory(): List<String> {
        val json = prefs.getString(KEY_SANCTION_HISTORY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addSanction(reason: String) {
        val list = getSanctionHistory().toMutableList()
        list.add(0, reason)
        val arr = JSONArray(list.take(10))
        prefs.edit().putString(KEY_SANCTION_HISTORY, arr.toString()).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
