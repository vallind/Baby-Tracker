package com.babytracker

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import com.babytracker.core.di.appModule
import com.babytracker.core.di.databaseModule
import com.babytracker.core.di.syncModule
import com.babytracker.core.sync.SyncEngine
import com.babytracker.core.sync.SyncSettings
import com.babytracker.core.sync.SyncTrigger
import com.babytracker.core.sync.SyncWorker
import com.babytracker.core.util.AppLogTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.io.File
import java.io.FileWriter

class BabyTrackerApp : Application() {
    lateinit var appLogTree: AppLogTree
        private set

    private val bgScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val crashLog = File(filesDir, "crash.log")
        try {
            appLogTree = AppLogTree(this)
            appLogTree.enabled = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getBoolean("log_capture_enabled", false)
            Timber.plant(appLogTree)
        } catch (e: Throwable) {
            FileWriter(crashLog, true).use { it.append("Timber init failed: ${e.message}\n") }
        }

        try {
            val koin = startKoin {
                androidContext(this@BabyTrackerApp)
                modules(appModule, databaseModule, syncModule)
            }.koin

            // 延后至首帧之后启动，不阻塞启动速度
            Handler(Looper.getMainLooper()).post {
                bgScope.launch {
                    try {
                        WorkManager.initialize(this@BabyTrackerApp, Configuration.Builder()
                            .setWorkerFactory(object : WorkerFactory() {
                                override fun createWorker(
                                    appContext: Context,
                                    className: String,
                                    workerParams: WorkerParameters,
                                ): ListenableWorker? {
                                    if (className == SyncWorker::class.java.name) {
                                        return SyncWorker(
                                            appContext, workerParams,
                                            koin.get<SyncEngine>(),
                                            koin.get<SyncSettings>(),
                                            koin.get<AuthService>(),
                                            koin.get<FamilyService>(),
                                        )
                                    }
                                    return null
                                }
                            })
                            .build())
                    } catch (e: Throwable) {
                        Log.e("BabyTracker", "WorkManager init failed", e)
                        FileWriter(crashLog, true).use { it.append("WorkManager: ${e.message}\n") }
                    }
                }

                try {
                    koin.get<SyncTrigger>().start()
                } catch (e: Throwable) {
                    Log.e("BabyTracker", "SyncTrigger start failed", e)
                    FileWriter(crashLog, true).use { it.append("SyncTrigger: ${e.message}\n") }
                }

                try {
                    ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
                        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
                        fun onBackground() {
                            bgScope.launch {
                                try { koin.get<SyncTrigger>().onAppBackgrounded() } catch (_: Throwable) {}
                            }
                        }
                    })
                } catch (e: Throwable) {
                    Log.e("BabyTracker", "ProcessLifecycleOwner failed", e)
                    FileWriter(crashLog, true).use { it.append("Lifecycle: ${e.message}\n") }
                }
            }
        } catch (e: Throwable) {
            Log.e("BabyTracker", "Startup failed", e)
            FileWriter(crashLog, true).use { it.append("Startup: ${e.message}\n${e.stackTraceToString()}\n") }
        }
    }
}
