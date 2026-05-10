package com.example.mymy.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseConfig {
    const val SUPABASE_URL = "https://sbizrtjugvtcajdkkiak.supabase.co"
    const val SUPABASE_ANON_KEY = "sb_publishable_c9uXXvtiT1W0hLdnzwltXg_qpadPcmb" // keep full key

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Functions)
    }
}
