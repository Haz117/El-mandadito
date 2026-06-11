package com.elmandadito.app.network

import com.elmandadito.app.BuildConfig

object SupabaseConfig {
    val URL      get() = BuildConfig.SUPABASE_URL
    val ANON_KEY get() = BuildConfig.SUPABASE_ANON_KEY
}
