package com.elmandadito.app.data

import android.content.Context
import android.content.SharedPreferences

object SearchHistoryManager {
    private const val PREFS = "search_history_prefs"
    private const val KEY = "queries"
    private const val MAX = 5

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun add(query: String) {
        val q = query.trim()
        if (q.isBlank() || !::prefs.isInitialized) return
        val list = getAll().toMutableList()
        list.remove(q)
        list.add(0, q)
        if (list.size > MAX) list.removeAt(list.lastIndex)
        prefs.edit().putString(KEY, list.joinToString("|||")).apply()
    }

    fun getAll(): List<String> {
        if (!::prefs.isInitialized) return emptyList()
        val raw = prefs.getString(KEY, "") ?: ""
        return raw.split("|||").filter { it.isNotBlank() }
    }

    fun remove(query: String) {
        if (!::prefs.isInitialized) return
        val list = getAll().toMutableList()
        list.remove(query)
        prefs.edit().putString(KEY, list.joinToString("|||")).apply()
    }

    fun clear() {
        if (!::prefs.isInitialized) return
        prefs.edit().remove(KEY).apply()
    }
}
