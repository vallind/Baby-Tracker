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
        val result = coordinator.syncNow(SyncReason.BACKGROUND) ?: return Result.success()
        return if (result.failures.isEmpty()) Result.success() else Result.retry()
    }
}
