package com.babytracker.core.di

import android.content.Context
import android.content.SharedPreferences
import com.babytracker.core.backup.BackupManager
import com.babytracker.core.database.AppDatabase
import com.babytracker.designsystem.theme.ThemeController
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.*
import com.babytracker.feature.stats.StatsViewModel
import com.babytracker.feature.home.HomeViewModel
import com.babytracker.feature.message.MessageViewModel
import com.babytracker.feature.development.DevelopmentAssessmentViewModel
import com.babytracker.feature.reminder.ReminderViewModel
import com.babytracker.feature.timeline.TimelineViewModel
import com.babytracker.feature.auth.LoginViewModel
import com.babytracker.feature.settings.SettingsViewModel
import com.babytracker.feature.family.FamilyViewModel
import com.babytracker.core.data.FamilyService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SharedPreferences> { androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    single { ThemeController(get()) }
    single { BabyController(get(), get()) }
    single { BackupManager(get()) }
    single<BabyRepository> { BabyRepositoryImpl(get()) }
    single<FeedingRepository> { FeedingRepositoryImpl(get()) }
    single<SleepRepository> { SleepRepositoryImpl(get()) }
    single<GrowthRepository> { GrowthRepositoryImpl(get()) }
    single<VaccinationRepository> { VaccinationRepositoryImpl(get()) }
    single<HealthRepository> { HealthRepositoryImpl(get()) }
    single<DiaperRepository> { DiaperRepositoryImpl(get()) }
    single<MessageRepository> { MessageRepositoryImpl(get()) }
    single<DevelopmentAssessmentRepository> { DevelopmentAssessmentRepositoryImpl(get()) }
    single<ReminderRepository> { ReminderRepositoryImpl(get()) }
    viewModel { StatsViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { MessageViewModel(get()) }
    viewModel { DevelopmentAssessmentViewModel(get(), get()) }
    viewModel { ReminderViewModel(get()) }
    viewModel { TimelineViewModel(get(), get(), get(), get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), androidContext()) }
    viewModel { FamilyViewModel(get()) }
}

val databaseModule = module {
    single { AppDatabase.get(androidContext()) }
    single { get<AppDatabase>().babyDao() }
    single { get<AppDatabase>().feedingDao() }
    single { get<AppDatabase>().sleepDao() }
    single { get<AppDatabase>().growthDao() }
    single { get<AppDatabase>().vaccinationDao() }
    single { get<AppDatabase>().healthRecordDao() }
    single { get<AppDatabase>().diaperDao() }
    single { get<AppDatabase>().backupConfigDao() }
    single { get<AppDatabase>().messageDao() }
    single { get<AppDatabase>().developmentAssessmentDao() }
    single { get<AppDatabase>().reminderDao() }
    single { get<AppDatabase>().syncMetadataDao() }
}

// ── Supabase 同步模块 ──
val syncModule = module {
    single { com.babytracker.core.sync.SupabaseProvider.client }
    single { com.babytracker.core.auth.AuthService(get(), get()) }
    single { com.babytracker.core.sync.SyncEngine(get(), get()) }
    single { com.babytracker.core.sync.RealtimeManager(get(), get(), get()) }
    single { FamilyService(get()) }
}
