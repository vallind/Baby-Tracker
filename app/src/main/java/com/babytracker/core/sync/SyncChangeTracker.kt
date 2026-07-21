package com.babytracker.core.sync

import com.babytracker.core.data.FamilyService
import com.babytracker.core.database.dao.SyncMetadataDao
import com.babytracker.core.database.entity.SyncMetadataEntity

/** 写入同步元数据时立即固化家庭归属，禁止后续切换家庭时重新认领。 */
class SyncChangeTracker(
    private val dao: SyncMetadataDao,
    private val familyService: FamilyService,
) {
    suspend fun pendingChange(tableName: String, localId: Int, uuid: String?, updatedAt: Long) {
        val existing = dao.getByTableAndId(tableName, localId)
        val familyId = resolveSyncFamilyId(existing?.familyId, familyService.currentFamily.value?.id)
        dao.insert(
            (existing ?: SyncMetadataEntity(tableName = tableName, localId = localId)).copy(
                remoteUuid = uuid ?: existing?.remoteUuid,
                syncStatus = "pending",
                updatedAt = updatedAt,
                familyId = familyId,
                retryCount = 0,
                nextRetryAt = 0,
                lastError = null,
            )
        )
    }
}

internal fun resolveSyncFamilyId(existingFamilyId: String?, currentFamilyId: String?): String? =
    existingFamilyId ?: currentFamilyId
