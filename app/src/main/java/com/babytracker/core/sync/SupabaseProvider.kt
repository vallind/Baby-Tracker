package com.babytracker.core.sync

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Supabase Client 单例提供者。
 *
 * 配置通过 BuildConfig 注入，避免硬编码密钥。
 * 需要在 app/build.gradle.kts 中配置 buildConfigField。
 */
object SupabaseProvider {

    // 注：生产环境应通过 BuildConfig 注入，避免硬编码密钥
    private const val SUPABASE_URL = "https://kzwmcbdgmngyqmjgmjne.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt6d21jYmRnbW5neXFtamdtam5lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIxMjg5MzUsImV4cCI6MjA5NzcwNDkzNX0.GTwQDfE8pNF_W--PLmVc3PxjemDTxUvDwNfr1ONQzLA"

    val client: SupabaseClient by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
            install(Postgrest)
            install(Auth) {
                // 启动时自动从本地存储恢复登录态
                autoLoadFromStorage = true
                // 自动刷新过期的 token（无需用户重新登录）
                alwaysAutoRefresh = true
            }
            install(Realtime)
            install(Storage)
        }
    }
}
