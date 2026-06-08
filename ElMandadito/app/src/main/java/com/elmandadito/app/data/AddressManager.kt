package com.elmandadito.app.data

import android.content.Context
import android.content.SharedPreferences

object AddressManager {
    private const val PREFS = "address_prefs"
    private const val KEY_SELECTED = "selected_address"
    private const val KEY_CUSTOM = "custom_address"
    private const val DEFAULT_CUSTOM = "Plaza Satélite, Naucalpan"

    private lateinit var prefs: SharedPreferences

    val fixedAddresses = listOf(
        "Casa" to "Colonia Centro, CDMX",
        "Trabajo" to "Av. Insurgentes Sur 123, CDMX"
    )

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun getSelected(): Int = prefs.getInt(KEY_SELECTED, 0)

    fun setSelected(index: Int) = prefs.edit().putInt(KEY_SELECTED, index).apply()

    fun getCustomAddress(): String = prefs.getString(KEY_CUSTOM, DEFAULT_CUSTOM) ?: DEFAULT_CUSTOM

    fun setCustomAddress(addr: String) {
        prefs.edit().putString(KEY_CUSTOM, addr.trim().ifBlank { DEFAULT_CUSTOM }).apply()
    }

    fun getSelectedLabel(): String = when (getSelected()) {
        0 -> fixedAddresses[0].second
        1 -> fixedAddresses[1].second
        else -> getCustomAddress()
    }
}
