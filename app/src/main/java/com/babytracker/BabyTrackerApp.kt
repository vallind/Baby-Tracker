package com.babytracker

import android.app.Application
import com.babytracker.core.di.appModule
import com.babytracker.core.di.databaseModule
import com.babytracker.core.di.syncModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BabyTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BabyTrackerApp)
            modules(appModule, databaseModule, syncModule)
        }
    }
}
