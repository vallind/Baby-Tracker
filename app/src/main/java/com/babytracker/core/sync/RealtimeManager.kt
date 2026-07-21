package com.babytracker.core.sync

import com.babytracker.core.database.AppDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

/**
 * Supabase Realtime 订阅管理器。
 *
 * 监听 Supabase 数据库中所有业务表的 INSERT/UPDATE/DELETE 变更，
 * 自动将远程变更应用到本地 Room 数据库。
 */
class RealtimeManager(
    private val db: AppDatabase,
    private val supabase: SupabaseClient,
    private val syncEngine: SyncEngine,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow(RealtimeState.DISCONNECTED)
    val connectionState: StateFlow<RealtimeState> = _connectionState.asStateFlow()

    /** 家庭成员变更事件（新成员加入 / 角色变更 / 移除），供 FamilyViewModel 监听并刷新 UI */
    private val _familyMembersChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val familyMembersChanged: SharedFlow<Unit> = _familyMembersChanged.asSharedFlow()

    private var channel: io.github.jan.supabase.realtime.RealtimeChannel? = null

    /** 订阅所有业务表的变更（切换家庭时重新调用，确保先断开旧频道再建新频道） */
    fun subscribeAll() {
        scope.launch {
            // 先断开旧频道，避免订阅冲突和 RLS 上下文过期
            disconnect()
            try {
                _connectionState.value = RealtimeState.CONNECTING

                val ch = supabase.channel("db-changes")

                // 为每张表订阅变更（监听所有事件类型：INSERT/UPDATE/DELETE）
                val tables = listOf(
                    "babies", "feedings", "sleeps", "growths", "vaccinations",
                    "health_records", "diapers", "development_assessments", "reminders",
                    "family_members",
                )

                for (tableName in tables) {
                    val changeFlow = ch.postgresChangeFlow<PostgresAction>(
                        schema = "public",
                        filter = { table = tableName },
                    )

                    changeFlow.onEach { action ->
                        handleRealtimeChange(tableName, action)
                    }.launchIn(scope)
                }

                ch.subscribe(blockUntilSubscribed = true)
                channel = ch
                _connectionState.value = RealtimeState.CONNECTED
            } catch (e: Exception) {
                _connectionState.value = RealtimeState.ERROR
            }
        }
    }

    /** 取消所有订阅（异步，供外部调用） */
    fun unsubscribe() {
        scope.launch { disconnect() }
    }

    /** 断开当前频道（suspend，内部在协程内顺序执行） */
    private suspend fun disconnect() {
        try {
            channel?.unsubscribe()
            channel = null
            _connectionState.value = RealtimeState.DISCONNECTED
        } catch (_: Exception) { }
    }

    /** 处理来自 Realtime 的变更事件 */
    private suspend fun handleRealtimeChange(tableName: String, action: PostgresAction) {
        try {
            // family_members 表不在 Room 中，仅通知 ViewModel 刷新 UI
            if (tableName == "family_members") {
                _familyMembersChanged.tryEmit(Unit)
                return
            }

            when (action) {
                is PostgresAction.Insert -> {
                    val record = action.record as? JsonObject ?: return
                    syncEngine.applyRemoteChange(tableName, record)
                }
                is PostgresAction.Update -> {
                    val record = action.record as? JsonObject ?: return
                    syncEngine.applyRemoteChange(tableName, record)
                }
                is PostgresAction.Delete -> {
                    val oldRecord = action.oldRecord as? JsonObject ?: return
                    val uuid = oldRecord["uuid"]?.toString()?.removeSurrounding("\"") ?: return
                    val now = System.currentTimeMillis()
                    // 通过 uuid 查找并软删除本地记录
                    softDeleteLocal(tableName, uuid, now)
                }
                is PostgresAction.Select -> { /* 不处理 select 事件 */ }
            }
        } catch (_: Exception) {
            // 单条记录处理失败不阻塞其他变更
        }
    }

    /** 根据 uuid 软删除本地记录 */
    private suspend fun softDeleteLocal(tableName: String, uuid: String, deletedAt: Long) {
        when (tableName) {
            "babies" -> db.babyDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
            "feedings" -> db.feedingDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
            "sleeps" -> db.sleepDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
            "growths" -> db.growthDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
            "vaccinations" -> db.vaccinationDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
            "health_records" -> db.healthRecordDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
            "diapers" -> db.diaperDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
            "development_assessments" -> db.developmentAssessmentDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
            "reminders" -> db.reminderDao().softDeleteByUuid(uuid, deletedAt, deletedAt)
        }
    }
}

/** Realtime 连接状态 */
enum class RealtimeState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }
