package com.babytracker

import android.app.Application
import android.content.Context
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
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class BabyTrackerApp : Application() {
    lateinit var appLogTree: AppLogTree
        private set

    override fun onCreate() {
        super.onCreate()
        appLogTree = AppLogTree(this)
        appLogTree.enabled = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getBoolean("log_capture_enabled", false)
        Timber.plant(appLogTree)
        val koin = startKoin {
            androidContext(this@BabyTrackerApp)
            modules(appModule, databaseModule, syncModule)
        }.koin

        WorkManager.initialize(this, Configuration.Builder()
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

        koin.get<SyncTrigger>().start()

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
            fun onBackground() {
                koin.get<SyncTrigger>().onAppBackgrounded()
            }
        })
    }
}
