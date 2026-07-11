package com.babytracker

import android.app.Application
import android.content.Context
import com.babytracker.core.util.AppLogTree
import com.babytracker.core.di.appModule
import com.babytracker.core.di.databaseModule
import com.babytracker.core.di.syncModule
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
        startKoin {
            androidContext(this@BabyTrackerApp)
            modules(appModule, databaseModule, syncModule)
        }
    }
}
