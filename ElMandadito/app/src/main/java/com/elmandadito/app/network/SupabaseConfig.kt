package com.elmandadito.app.network

object SupabaseConfig {
    const val URL = "https://mcqqwjtmyqitbdwiohih.supabase.co/"

    // ⚠️  REEMPLAZA con la clave ANON de tu dashboard:
    //     Supabase → Settings → API → "anon public"  (empieza con sb_publishable_...)
    //     NUNCA uses la service_role key (sb_secret_...) aquí
    const val ANON_KEY = "REEMPLAZA_CON_TU_CLAVE_ANON"
}
