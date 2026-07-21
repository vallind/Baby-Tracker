package com.babytracker

import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.babytracker.core.util.AppLogTree
import com.babytracker.core.di.appModule
import com.babytracker.core.di.databaseModule
import com.babytracker.core.di.syncModule
import com.babytracker.core.sync.SyncCoordinator
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
        val syncCoordinator = koin.get<SyncCoordinator>()
        syncCoordinator.start()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                syncCoordinator.onAppBackgrounded()
            }
        })
    }
}
