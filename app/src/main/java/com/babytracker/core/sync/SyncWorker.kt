package com.babytracker.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    app: Context,
    params: WorkerParameters,
) : CoroutineWorker(app, params), KoinComponent {

    private val syncEngine: SyncEngine by inject()
    private val syncSettings: SyncSettings by inject()
    private val authService: AuthService by inject()
    private val familyService: FamilyService by inject()

    override suspend fun doWork(): Result {
        val config = syncSettings.config.value
        if (!config.autoSync) return Result.success()
        val userId = authService.verifiedUserId()
            ?: return if (authService.hasCachedSession() && runAttemptCount < 3) Result.retry() else Result.success()

        runCatching { familyService.refreshForUser(userId) }
            .getOrElse { return if (runAttemptCount < 3) Result.retry() else Result.failure() }
        syncEngine.currentFamilyId = familyService.sessionState.value.verifiedFamilyForSync?.id
        if (syncEngine.currentFamilyId == null) return Result.success()

        return try {
            syncEngine.fullSync()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
