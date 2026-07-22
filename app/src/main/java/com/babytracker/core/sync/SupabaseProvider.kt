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
 * 这里只保存客户端可公开的项目地址和 API Key，不包含服务端密钥。
 */
object SupabaseProvider {

    // 当前为客户端可公开的 legacy anon key，不得在这里放 service_role 或 secret key。
    const val PROJECT_URL = "https://kzwmcbdgmngyqmjgmjne.supabase.co"
    const val PUBLIC_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt6d21jYmRnbW5neXFtamdtam5lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIxMjg5MzUsImV4cCI6MjA5NzcwNDkzNX0.GTwQDfE8pNF_W--PLmVc3PxjemDTxUvDwNfr1ONQzLA"

    val client: SupabaseClient by lazy {
        createSupabaseClient(PROJECT_URL, PUBLIC_API_KEY) {
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
