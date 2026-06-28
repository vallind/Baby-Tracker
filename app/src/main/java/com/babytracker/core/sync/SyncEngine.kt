package com.babytracker.core.sync

import com.babytracker.core.database.AppDatabase
import com.babytracker.core.database.entity.*
import com.babytracker.core.database.dao.SyncMetadataDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 双向增量同步引擎。
 *
 * 策略：
 * - 上行（push）：将本地 syncStatus=pending 的记录通过 Postgrest upsert 到 Supabase。
 * - 下行（pull）：根据 lastSyncAt 拉取云端变更，写入本地 Room。
 * - 冲突处理：Last-Write-Wins（比较 updatedAt，更新者胜出）。
 * - 所有同步操作通过 sync_metadata 表跟踪状态。
 *
 * 使用方式：
 * ```
 * val engine = SyncEngine(db, supabaseClient)
 * engine.push()  // 上行同步
 * engine.pull()  // 下行同步
 * engine.fullSync()  // 完整双向同步
 * ```
 */
class SyncEngine(
    private val db: AppDatabase,
    private val supabase: SupabaseClient,
) {
    private val syncMeta: SyncMetadataDao get() = db.syncMetadataDao()

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    /** 当前家庭 ID（登录+加入家庭后设置），push 时自动注入到每条记录 */
    var currentFamilyId: String? = null

    /** 表名到 DAO 操作的映射 */
    private suspend fun getEntityDao(tableName: String): EntityDao<*>? = when (tableName) {
        "babies" -> EntityDao(
            getById = { id -> db.babyDao().getById(id) },
            getByUuid = { uuid -> db.babyDao().getByUuid(uuid) },
            upsert = { json -> db.babyDao().insert(parseBaby(json)) },
            updateLocal = { entity -> db.babyDao().update(entity as BabyEntity) },
        )
        "feedings" -> EntityDao(
            getById = { id -> db.feedingDao().getById(id) },
            getByUuid = { uuid -> db.feedingDao().getByUuid(uuid) },
            upsert = { json -> db.feedingDao().insert(parseFeeding(json)) },
            updateLocal = { entity -> db.feedingDao().update(entity as FeedingEntity) },
        )
        "sleeps" -> EntityDao(
            getById = { id -> db.sleepDao().getById(id) },
            getByUuid = { uuid -> db.sleepDao().getByUuid(uuid) },
            upsert = { json -> db.sleepDao().insert(parseSleep(json)) },
            updateLocal = { entity -> db.sleepDao().update(entity as SleepEntity) },
        )
        "growths" -> EntityDao(
            getById = { id -> db.growthDao().getById(id) },
            getByUuid = { uuid -> db.growthDao().getByUuid(uuid) },
            upsert = { json -> db.growthDao().insert(parseGrowth(json)) },
            updateLocal = { entity -> db.growthDao().update(entity as GrowthEntity) },
        )
        "vaccinations" -> EntityDao(
            getById = { id -> db.vaccinationDao().getById(id) },
            getByUuid = { uuid -> db.vaccinationDao().getByUuid(uuid) },
            upsert = { json -> db.vaccinationDao().insert(parseVaccination(json)) },
            updateLocal = { entity -> db.vaccinationDao().update(entity as VaccinationEntity) },
        )
        "health_records" -> EntityDao(
            getById = { id -> db.healthRecordDao().getById(id) },
            getByUuid = { uuid -> db.healthRecordDao().getByUuid(uuid) },
            upsert = { json -> db.healthRecordDao().insert(parseHealthRecord(json)) },
            updateLocal = { entity -> db.healthRecordDao().update(entity as HealthRecordEntity) },
        )
        "diapers" -> EntityDao(
            getById = { id -> db.diaperDao().getById(id) },
            getByUuid = { uuid -> db.diaperDao().getByUuid(uuid) },
            upsert = { json -> db.diaperDao().insert(parseDiaper(json)) },
            updateLocal = { entity -> db.diaperDao().update(entity as DiaperEntity) },
        )
        "messages" -> EntityDao(
            getById = { id -> db.messageDao().getById(id.toLong())?.let { it } },
            getByUuid = { uuid -> db.messageDao().getByUuid(uuid)?.let { it } },
            upsert = { json -> db.messageDao().insert(parseMessage(json)) },
            updateLocal = { entity -> db.messageDao().update(entity as MessageEntity) },
        )
        "development_assessments" -> EntityDao(
            getById = { id -> db.developmentAssessmentDao().getById(id) },
            getByUuid = { uuid -> db.developmentAssessmentDao().getByUuid(uuid) },
            upsert = { json -> db.developmentAssessmentDao().insert(parseDevAssessment(json)) },
            updateLocal = { entity -> db.developmentAssessmentDao().update(entity as DevelopmentAssessmentEntity) },
        )
        "reminders" -> EntityDao(
            getById = { id -> db.reminderDao().getById(id) },
            getByUuid = { uuid -> db.reminderDao().getByUuid(uuid) },
            upsert = { json -> db.reminderDao().insert(parseReminder(json)) },
            updateLocal = { entity -> db.reminderDao().update(entity as ReminderEntity) },
        )
        else -> null
    }

    // ================================================================
    // 上行同步：本地 → 云端
    // ================================================================

    /**
     * 将本地所有 pending 变更推送到 Supabase。
     * 使用 upsert 策略（冲突时用云端 uuid 匹配，updatedAt 决定覆盖）。
     */
    /** 上行同步，返回实际推送的记录数 */
    suspend fun push(): Int {
        if (_syncState.value == SyncState.SYNCING) return 0
        _syncState.value = SyncState.SYNCING
        var pushed = 0
        try {
            val pendingChanges = syncMeta.getPendingChanges()
            if (pendingChanges.isEmpty()) {
                _syncState.value = SyncState.IDLE
                return 0
            }
            _syncState.value = SyncState.PUSHING

            for (meta in pendingChanges) {
                try {
                    val dao = getEntityDao(meta.tableName) ?: continue
                    val localEntity = dao.getById(meta.localId) ?: continue

                    val basePayload = entityToJson(meta.tableName, localEntity)
                    val payload = injectFamilyId(basePayload)

                    val remoteUuid = meta.remoteUuid ?: payload["uuid"]?.toString()?.removeSurrounding("\"")
                    if (remoteUuid != null) {
                        supabase.postgrest.from(meta.tableName)
                            .upsert(payload) { onConflict = "uuid" }
                        syncMeta.markSynced(meta.id, remoteUuid, System.currentTimeMillis())
                    } else {
                        supabase.postgrest.from(meta.tableName).insert(payload)
                        syncMeta.markSynced(meta.id, payload["uuid"]?.toString()?.removeSurrounding("\""), System.currentTimeMillis())
                    }
                    pushed++
                } catch (e: Exception) {
                    syncMeta.markConflict(meta.id, System.currentTimeMillis())
                }
            }
            syncMeta.updateLastSyncAt(System.currentTimeMillis())
        } finally {
            _syncState.value = SyncState.IDLE
        }
        return pushed
    }

    /** 下行同步，返回实际拉取的记录数 */
    suspend fun pull(): Int {
        if (_syncState.value == SyncState.SYNCING) return 0
        val fid = currentFamilyId ?: return 0  // 无家庭上下文，不拉取
        _syncState.value = SyncState.SYNCING
        var pulled = 0
        try {
            _syncState.value = SyncState.PULLING
            val lastSyncAt = syncMeta.getLastSyncAt()
            val tables = listOf(
                "babies", "feedings", "sleeps", "growths", "vaccinations",
                "health_records", "diapers", "messages", "development_assessments", "reminders",
            )
            for (tableName in tables) {
                try {
                    val result: List<JsonObject> = supabase.postgrest.from(tableName)
                        .select(columns = Columns.ALL) {
                            filter {
                                eq("family_id", fid)
                                if (lastSyncAt != null) gte("updatedAt", lastSyncAt)
                            }
                        }
                        .decodeList<JsonObject>()
                    for (row in result) { applyRemoteChange(tableName, row); pulled++ }
                } catch (_: Exception) { }
            }
            syncMeta.updateLastSyncAt(System.currentTimeMillis())
        } finally {
            _syncState.value = SyncState.IDLE
        }
        return pulled
    }

    /** 查询当前 pending 记录数（仅诊断用） */
    suspend fun pendingCount(): Int = syncMeta.getPendingChanges().size

    /**
     * 清除全局 lastSyncAt 时间戳。
     * 下次 pull() 将执行全量拉取（而非增量），用于切换/加入新家庭时同步历史数据。
     */
    suspend fun resetLastSync() {
        syncMeta.clearLastSyncAt()
    }

    /** 全量同步，返回 [推送数, 拉取数] */
    suspend fun fullSync(): Pair<Int, Int> {
        val pushed = push()
        val pulled = pull()
        return pushed to pulled
    }

    /**
     * 将本地所有已有的、未标记 pending 的记录标记为 pending。
     * 用于存量数据首次同步——之前创建的数据没有 sync_metadata 记录。
     */
    suspend fun markExistingPending() {
        val db = db.openHelper.writableDatabase
        val tables = listOf(
            "babies", "feedings", "sleeps", "growths", "vaccinations",
            "health_records", "diapers", "messages", "development_assessments", "reminders",
        )
        db.beginTransaction()
        try {
            for (table in tables) {
                try {
                    db.execSQL("UPDATE $table SET uuid = lower(hex(randomblob(16))) WHERE uuid IS NULL AND deletedAt IS NULL")
                    // 将之前失败的 conflict 重置为 pending
                    db.execSQL("UPDATE sync_metadata SET syncStatus='pending' WHERE tableName='$table' AND syncStatus='conflict'")
                    db.execSQL("""
                        INSERT INTO sync_metadata (tableName, localId, remoteUuid, syncStatus, updatedAt)
                        SELECT '$table', id, uuid, 'pending', COALESCE(updatedAt, 0)
                        FROM $table
                        WHERE deletedAt IS NULL AND uuid IS NOT NULL
                          AND id NOT IN (SELECT localId FROM sync_metadata WHERE tableName = '$table')
                    """)
                } catch (_: Exception) { /* 单表失败不影响其他表 */ }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ================================================================
    // 内部：将远程变更应用到本地
    // ================================================================

    /** 包内可见：供 RealtimeManager 调用，将远程变更写入本地 Room */
    internal suspend fun applyRemoteChange(tableName: String, remoteRow: JsonObject) {
        val dao = getEntityDao(tableName) ?: return
        val remoteUuid = remoteRow["uuid"]?.toString()?.removeSurrounding("\"") ?: return
        val remoteUpdatedAt = remoteRow["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L

        try {
            // 按 uuid 查找本地记录，比较 updatedAt 决定是否覆盖（LWW）
            val localEntity = dao.getByUuid(remoteUuid)
            if (localEntity != null) {
                val localUpdatedAt = getUpdatedAt(localEntity)
                // 本地版本更新 → 忽略远程变更
                if (localUpdatedAt >= remoteUpdatedAt) return
            }

            // 远程版本更新（或本地无此记录）→ 写入本地
            val insertedId = dao.upsert(remoteRow)
            // 记录同步元数据
            syncMeta.insert(
                SyncMetadataEntity(
                    tableName = tableName,
                    localId = insertedId.toInt(),
                    remoteUuid = remoteUuid,
                    syncStatus = "synced",
                    updatedAt = remoteUpdatedAt,
                    lastSyncAt = System.currentTimeMillis(),
                )
            )
        } catch (_: Exception) {
            // 记录冲突
        }
    }

    /** 从任意 Entity 中提取 updatedAt 字段（通过反射，LWW 冲突比较用） */
    private fun getUpdatedAt(entity: Any): Long {
        return try {
            entity::class.java.getDeclaredField("updatedAt").apply { isAccessible = true }
                .getLong(entity)
        } catch (_: Exception) {
            0L
        }
    }

    /** 向 JSON payload 注入 family_id，满足 RLS 家庭隔离策略 */
    private fun injectFamilyId(base: JsonObject): JsonObject {
        val fid = currentFamilyId ?: return base
        return buildJsonObject {
            base.forEach { (key, value) -> put(key, value) }
            put("family_id", JsonPrimitive(fid))
        }
    }

    // ================================================================
    // Entity ↔ JSON 转换（简化版，按表名分发）
    // ================================================================

    /** 通过本地 babyId 获取宝宝 uuid（push 时用） */
    private suspend fun babyUuid(localBabyId: Int): String =
        db.babyDao().getById(localBabyId)?.uuid ?: ""

    /** 通过宝宝 uuid 获取本地 babyId（pull 时用），找不到返回 0 */
    private suspend fun resolveLocalBabyId(uuid: String?): Int =
        if (uuid != null) db.babyDao().getByUuid(uuid)?.id ?: 0 else 0

    private suspend fun entityToJson(tableName: String, entity: Any): JsonObject {
        return when (tableName) {
            "babies" -> {
                val e = entity as BabyEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("name", JsonPrimitive(e.name))
                    put("gender", JsonPrimitive(e.gender))
                    put("birthDate", JsonPrimitive(e.birthDate))
                    e.birthWeight?.let { put("birthWeight", JsonPrimitive(it)) }
                    e.birthHeight?.let { put("birthHeight", JsonPrimitive(it)) }
                    e.avatarPath?.let { put("avatarPath", JsonPrimitive(it)) }
                    put("createdAt", JsonPrimitive(e.createdAt))
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "feedings" -> {
                val e = entity as FeedingEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("babyId", JsonPrimitive(babyUuid(e.babyId)))
                    put("type", JsonPrimitive(e.type))
                    e.amountMl?.let { put("amountMl", JsonPrimitive(it)) }
                    e.durationMin?.let { put("durationMin", JsonPrimitive(it)) }
                    e.breastSide?.let { put("breastSide", JsonPrimitive(it)) }
                    e.foodName?.let { put("foodName", JsonPrimitive(it)) }
                    e.amountG?.let { put("amountG", JsonPrimitive(it)) }
                    e.brand?.let { put("brand", JsonPrimitive(it)) }
                    e.note?.let { put("note", JsonPrimitive(it)) }
                    put("timestamp", JsonPrimitive(e.timestamp))
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "sleeps" -> {
                val e = entity as SleepEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("babyId", JsonPrimitive(babyUuid(e.babyId)))
                    put("type", JsonPrimitive(e.type))
                    put("startTime", JsonPrimitive(e.startTime))
                    put("endTime", JsonPrimitive(e.endTime))
                    e.note?.let { put("note", JsonPrimitive(it)) }
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "growths" -> {
                val e = entity as GrowthEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("babyId", JsonPrimitive(babyUuid(e.babyId)))
                    put("type", JsonPrimitive(e.type))
                    put("value", JsonPrimitive(e.value))
                    put("measuredAt", JsonPrimitive(e.measuredAt))
                    e.note?.let { put("note", JsonPrimitive(it)) }
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "vaccinations" -> {
                val e = entity as VaccinationEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("babyId", JsonPrimitive(babyUuid(e.babyId)))
                    put("name", JsonPrimitive(e.name))
                    e.dose?.let { put("dose", JsonPrimitive(it)) }
                    e.scheduledDate?.let { put("scheduledDate", JsonPrimitive(it)) }
                    e.administeredDate?.let { put("administeredDate", JsonPrimitive(it)) }
                    put("status", JsonPrimitive(e.status))
                    e.note?.let { put("note", JsonPrimitive(it)) }
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "health_records" -> {
                val e = entity as HealthRecordEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("babyId", JsonPrimitive(babyUuid(e.babyId)))
                    put("category", JsonPrimitive(e.category))
                    put("description", JsonPrimitive(e.description))
                    e.doctorName?.let { put("doctorName", JsonPrimitive(it)) }
                    put("recordDate", JsonPrimitive(e.recordDate))
                    e.attachments?.let { put("attachments", JsonPrimitive(it)) }
                    e.note?.let { put("note", JsonPrimitive(it)) }
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "diapers" -> {
                val e = entity as DiaperEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("babyId", JsonPrimitive(babyUuid(e.babyId)))
                    put("type", JsonPrimitive(e.type))
                    put("timestamp", JsonPrimitive(e.timestamp))
                    e.note?.let { put("note", JsonPrimitive(it)) }
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "messages" -> {
                val e = entity as MessageEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("type", JsonPrimitive(e.type))
                    put("title", JsonPrimitive(e.title))
                    put("content", JsonPrimitive(e.content))
                    e.senderAvatar?.let { put("senderAvatar", JsonPrimitive(it)) }
                    put("createTime", JsonPrimitive(e.createTime))
                    put("isRead", JsonPrimitive(e.isRead))
                    put("extraData", JsonPrimitive(e.extraData))
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "development_assessments" -> {
                val e = entity as DevelopmentAssessmentEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("babyId", JsonPrimitive(babyUuid(e.babyId)))
                    put("assessDate", JsonPrimitive(e.assessDate))
                    put("babyAgeMonths", JsonPrimitive(e.babyAgeMonths))
                    put("grossMotor", JsonPrimitive(e.grossMotor))
                    put("fineMotor", JsonPrimitive(e.fineMotor))
                    put("language", JsonPrimitive(e.language))
                    put("social", JsonPrimitive(e.social))
                    put("cognitive", JsonPrimitive(e.cognitive))
                    put("note", JsonPrimitive(e.note))
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            "reminders" -> {
                val e = entity as ReminderEntity
                buildJsonObject {
                    put("uuid", JsonPrimitive(e.uuid))
                    put("babyId", JsonPrimitive(babyUuid(e.babyId)))
                    put("type", JsonPrimitive(e.type))
                    put("title", JsonPrimitive(e.title))
                    put("description", JsonPrimitive(e.description))
                    put("dueDate", JsonPrimitive(e.dueDate))
                    put("isDone", JsonPrimitive(e.isDone))
                    e.doneDate?.let { put("doneDate", JsonPrimitive(it)) }
                    put("isEnabled", JsonPrimitive(e.isEnabled))
                    put("repeatRule", JsonPrimitive(e.repeatRule))
                    put("updatedAt", JsonPrimitive(e.updatedAt))
                    e.deletedAt?.let { put("deletedAt", JsonPrimitive(it)) }
                }
            }
            else -> JsonObject(emptyMap())
        }
    }

    // ================================================================
    // JSON → Entity 解析（简化版）
    // ================================================================

    private fun parseBaby(json: JsonObject): BabyEntity = BabyEntity(
        id = 0,
        name = json["name"]?.toString()?.removeSurrounding("\"") ?: "",
        gender = json["gender"]?.toString()?.removeSurrounding("\"") ?: "",
        birthDate = json["birthDate"]?.toString()?.removeSurrounding("\"") ?: "",
        birthWeight = json["birthWeight"]?.toString()?.removeSurrounding("\"")?.toDoubleOrNull(),
        birthHeight = json["birthHeight"]?.toString()?.removeSurrounding("\"")?.toDoubleOrNull(),
        avatarPath = json["avatarPath"]?.toString()?.removeSurrounding("\""),
        createdAt = json["createdAt"]?.toString()?.removeSurrounding("\"") ?: "",
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private suspend fun parseFeeding(json: JsonObject): FeedingEntity = FeedingEntity(
        id = 0,
        babyId = resolveLocalBabyId(json["babyId"]?.toString()?.removeSurrounding("\"")),
        type = json["type"]?.toString()?.removeSurrounding("\"") ?: "",
        amountMl = json["amountMl"]?.toString()?.removeSurrounding("\"")?.toIntOrNull(),
        durationMin = json["durationMin"]?.toString()?.removeSurrounding("\"")?.toIntOrNull(),
        breastSide = json["breastSide"]?.toString()?.removeSurrounding("\""),
        foodName = json["foodName"]?.toString()?.removeSurrounding("\""),
        amountG = json["amountG"]?.toString()?.removeSurrounding("\"")?.toIntOrNull(),
        brand = json["brand"]?.toString()?.removeSurrounding("\""),
        note = json["note"]?.toString()?.removeSurrounding("\""),
        timestamp = json["timestamp"]?.toString()?.removeSurrounding("\"") ?: "",
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private suspend fun parseSleep(json: JsonObject): SleepEntity = SleepEntity(
        id = 0,
        babyId = resolveLocalBabyId(json["babyId"]?.toString()?.removeSurrounding("\"")),
        type = json["type"]?.toString()?.removeSurrounding("\"") ?: "",
        startTime = json["startTime"]?.toString()?.removeSurrounding("\"") ?: "",
        endTime = json["endTime"]?.toString()?.removeSurrounding("\"") ?: "",
        note = json["note"]?.toString()?.removeSurrounding("\""),
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private suspend fun parseGrowth(json: JsonObject): GrowthEntity = GrowthEntity(
        id = 0,
        babyId = resolveLocalBabyId(json["babyId"]?.toString()?.removeSurrounding("\"")),
        type = json["type"]?.toString()?.removeSurrounding("\"") ?: "",
        value = json["value"]?.toString()?.removeSurrounding("\"")?.toDoubleOrNull() ?: 0.0,
        measuredAt = json["measuredAt"]?.toString()?.removeSurrounding("\"") ?: "",
        note = json["note"]?.toString()?.removeSurrounding("\""),
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private suspend fun parseVaccination(json: JsonObject): VaccinationEntity = VaccinationEntity(
        id = 0,
        babyId = resolveLocalBabyId(json["babyId"]?.toString()?.removeSurrounding("\"")),
        name = json["name"]?.toString()?.removeSurrounding("\"") ?: "",
        dose = json["dose"]?.toString()?.removeSurrounding("\""),
        scheduledDate = json["scheduledDate"]?.toString()?.removeSurrounding("\""),
        administeredDate = json["administeredDate"]?.toString()?.removeSurrounding("\""),
        status = json["status"]?.toString()?.removeSurrounding("\"") ?: "pending",
        note = json["note"]?.toString()?.removeSurrounding("\""),
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private suspend fun parseHealthRecord(json: JsonObject): HealthRecordEntity = HealthRecordEntity(
        id = 0,
        babyId = resolveLocalBabyId(json["babyId"]?.toString()?.removeSurrounding("\"")),
        category = json["category"]?.toString()?.removeSurrounding("\"") ?: "",
        description = json["description"]?.toString()?.removeSurrounding("\"") ?: "",
        doctorName = json["doctorName"]?.toString()?.removeSurrounding("\""),
        recordDate = json["recordDate"]?.toString()?.removeSurrounding("\"") ?: "",
        attachments = json["attachments"]?.toString()?.removeSurrounding("\""),
        note = json["note"]?.toString()?.removeSurrounding("\""),
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private suspend fun parseDiaper(json: JsonObject): DiaperEntity = DiaperEntity(
        id = 0,
        babyId = resolveLocalBabyId(json["babyId"]?.toString()?.removeSurrounding("\"")),
        type = json["type"]?.toString()?.removeSurrounding("\"") ?: "",
        timestamp = json["timestamp"]?.toString()?.removeSurrounding("\"") ?: "",
        note = json["note"]?.toString()?.removeSurrounding("\""),
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private fun parseMessage(json: JsonObject): MessageEntity = MessageEntity(
        id = 0,
        type = json["type"]?.toString()?.removeSurrounding("\"") ?: "",
        title = json["title"]?.toString()?.removeSurrounding("\"") ?: "",
        content = json["content"]?.toString()?.removeSurrounding("\"") ?: "",
        senderAvatar = json["senderAvatar"]?.toString()?.removeSurrounding("\""),
        createTime = json["createTime"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        isRead = json["isRead"]?.toString()?.removeSurrounding("\"")?.toBooleanStrictOrNull() ?: false,
        extraData = json["extraData"]?.toString()?.removeSurrounding("\"") ?: "",
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private suspend fun parseDevAssessment(json: JsonObject): DevelopmentAssessmentEntity = DevelopmentAssessmentEntity(
        id = 0,
        babyId = resolveLocalBabyId(json["babyId"]?.toString()?.removeSurrounding("\"")),
        assessDate = json["assessDate"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        babyAgeMonths = json["babyAgeMonths"]?.toString()?.removeSurrounding("\"")?.toIntOrNull() ?: 0,
        grossMotor = json["grossMotor"]?.toString()?.removeSurrounding("\"")?.toIntOrNull() ?: 0,
        fineMotor = json["fineMotor"]?.toString()?.removeSurrounding("\"")?.toIntOrNull() ?: 0,
        language = json["language"]?.toString()?.removeSurrounding("\"")?.toIntOrNull() ?: 0,
        social = json["social"]?.toString()?.removeSurrounding("\"")?.toIntOrNull() ?: 0,
        cognitive = json["cognitive"]?.toString()?.removeSurrounding("\"")?.toIntOrNull() ?: 0,
        note = json["note"]?.toString()?.removeSurrounding("\"") ?: "",
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )

    private suspend fun parseReminder(json: JsonObject): ReminderEntity = ReminderEntity(
        id = 0,
        babyId = resolveLocalBabyId(json["babyId"]?.toString()?.removeSurrounding("\"")),
        type = json["type"]?.toString()?.removeSurrounding("\"") ?: "",
        title = json["title"]?.toString()?.removeSurrounding("\"") ?: "",
        description = json["description"]?.toString()?.removeSurrounding("\"") ?: "",
        dueDate = json["dueDate"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        isDone = json["isDone"]?.toString()?.removeSurrounding("\"")?.toBooleanStrictOrNull() ?: false,
        doneDate = json["doneDate"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
        isEnabled = json["isEnabled"]?.toString()?.removeSurrounding("\"")?.toBooleanStrictOrNull() ?: true,
        repeatRule = json["repeatRule"]?.toString()?.removeSurrounding("\"") ?: "",
        uuid = json["uuid"]?.toString()?.removeSurrounding("\""),
        updatedAt = json["updatedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull() ?: 0L,
        deletedAt = json["deletedAt"]?.toString()?.removeSurrounding("\"")?.toLongOrNull(),
    )
}

/**
 * 内部辅助类：封装单张表的增删改查操作。
 */
private class EntityDao<T>(
    val getById: suspend (Int) -> T?,
    val getByUuid: suspend (String) -> T?,
    val upsert: suspend (JsonObject) -> Long,
    val updateLocal: suspend (T) -> Unit,
)

/** 同步状态枚举 */
enum class SyncState { IDLE, SYNCING, PUSHING, PULLING }
