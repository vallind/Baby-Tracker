package com.babytracker.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService

class SyncWorker(
    app: Context,
    params: WorkerParameters,
    private val syncEngine: SyncEngine,
    private val syncSettings: SyncSettings,
    private val authService: AuthService,
    private val familyService: FamilyService,
) : CoroutineWorker(app, params) {

    override suspend fun doWork(): Result {
        val config = syncSettings.config.value
        if (!config.autoSync) return Result.success()
        if (!authService.isLoggedIn()) return Result.success()

        syncEngine.currentFamilyId = familyService.currentFamily.value?.id
        if (syncEngine.currentFamilyId == null) return Result.success()

        return try {
            syncEngine.fullSync()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
