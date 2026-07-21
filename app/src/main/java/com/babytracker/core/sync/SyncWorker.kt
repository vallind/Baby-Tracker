package com.babytracker.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.context.GlobalContext

/** 进程退出后由 WorkManager 提供的联网同步兜底。 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val coordinator = GlobalContext.get().get<SyncCoordinator>()
        val mode = runCatching { SyncMode.valueOf(inputData.getString(KEY_MODE) ?: SyncMode.FULL.name) }
            .getOrDefault(SyncMode.FULL)
        val result = coordinator.syncNow(SyncReason.BACKGROUND, mode) ?: return Result.success()
        return when {
            result.failures.isEmpty() -> Result.success()
            runAttemptCount < MAX_RETRIES -> Result.retry()
            else -> Result.failure()
        }
    }

    companion object {
        const val KEY_MODE = "sync_mode"
        private const val MAX_RETRIES = 5
    }
}
