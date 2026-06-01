package com.example.myapp.di

import com.example.myapp.data.datastore.NotificationPreference
import com.example.myapp.data.datastore.ThemePreference
import com.example.myapp.data.room.AppDatabase
import com.example.myapp.data.repository.BabyRepository
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.repository.HealthRepository
import com.example.myapp.data.repository.ReminderRepository
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.data.repository.VaccineRepository
import com.example.myapp.domain.feeding.AddFeedingUseCase
import com.example.myapp.domain.feeding.DeleteFeedingUseCase
import com.example.myapp.domain.growth.AddGrowthUseCase
import com.example.myapp.domain.growth.DeleteGrowthUseCase
import com.example.myapp.domain.sleep.AddSleepUseCase
import com.example.myapp.domain.sleep.DeleteSleepUseCase
import com.example.myapp.ui.feeding.FeedingViewModel
import com.example.myapp.ui.growth.GrowthViewModel
import com.example.myapp.ui.health.HealthViewModel
import com.example.myapp.ui.home.HomeViewModel
import com.example.myapp.ui.sleep.SleepViewModel
import com.example.myapp.ui.settings.SettingsViewModel
import com.example.myapp.ui.stats.StatsViewModel
import com.example.myapp.ui.vaccine.VaccineViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.build(androidContext()) }

    single { get<AppDatabase>().babyDao() }
    single { get<AppDatabase>().feedingDao() }
    single { get<AppDatabase>().sleepDao() }
    single { get<AppDatabase>().growthDao() }
    single { get<AppDatabase>().vaccineDao() }
    single { get<AppDatabase>().healthProfileDao() }
    single { get<AppDatabase>().reminderDao() }

    single { BabyRepository(get()) }
    single { FeedingRepository(get()) }
    single { SleepRepository(get()) }
    single { GrowthRepository(get()) }
    single { VaccineRepository(get()) }
    single { HealthRepository(get()) }
    single { ReminderRepository(get()) }

    single { AddFeedingUseCase(get()) }
    single { DeleteFeedingUseCase(get()) }
    single { AddSleepUseCase(get()) }
    single { DeleteSleepUseCase(get()) }
    single { AddGrowthUseCase(get()) }
    single { DeleteGrowthUseCase(get()) }

    single { ThemePreference(androidContext()) }
    single { NotificationPreference(androidContext()) }

    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { FeedingViewModel(get(), get(), get()) }
    viewModel { SleepViewModel(get(), get(), get()) }
    viewModel { GrowthViewModel(get(), get(), get()) }
    viewModel { VaccineViewModel(get()) }
    viewModel { HealthViewModel(get()) }
    viewModel { StatsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
