package com.babytracker.core.di

import android.content.Context
import android.content.SharedPreferences
import com.babytracker.core.ai.config.AiBootstrapClient
import com.babytracker.core.ai.config.AiConfigCoordinator
import com.babytracker.core.ai.config.AiConfigStore
import com.babytracker.core.ai.config.AiDeviceKeyStore
import com.babytracker.core.ai.provider.AiProviderClient
import com.babytracker.core.ai.provider.OpenAiCompatibleChatAdapter
import com.babytracker.core.ai.provider.OpenAiResponsesAdapter
import com.babytracker.core.ai.settings.AiSettingsStore
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
import com.babytracker.feature.ai.AiChatViewModel
import com.babytracker.feature.ai.AiContextBuilder
import com.babytracker.feature.ai.AiSettingsViewModel
import com.babytracker.core.data.FamilyService
import com.babytracker.core.sync.SyncSettings
import com.babytracker.core.sync.SyncTrigger
import com.babytracker.core.util.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val appModule = module {
    single<SharedPreferences> { androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    single { ThemeController(get()) }
    single { BabyController(get(), get()) }
    single { BackupManager(get()) }
    single { NetworkMonitor(androidContext()) }
    single<BabyRepository> { BabyRepositoryImpl(get(), get(), get(), get()) }
    single<FeedingRepository> { FeedingRepositoryImpl(get(), get(), get()) }
    single<SleepRepository> { SleepRepositoryImpl(get(), get(), get()) }
    single<GrowthRepository> { GrowthRepositoryImpl(get(), get(), get()) }
    single<VaccinationRepository> { VaccinationRepositoryImpl(get(), get(), get()) }
    single<HealthRepository> { HealthRepositoryImpl(get(), get(), get()) }
    single<DiaperRepository> { DiaperRepositoryImpl(get(), get(), get()) }
    single<MessageRepository> { MessageRepositoryImpl(get()) }
    single<DevelopmentAssessmentRepository> { DevelopmentAssessmentRepositoryImpl(get(), get(), get()) }
    single<ReminderRepository> { ReminderRepositoryImpl(get(), get(), get()) }
    single { SyncSettings(get()) }
    single { AiSettingsStore(get()) }
    viewModel { StatsViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { MessageViewModel(get()) }
    viewModel { DevelopmentAssessmentViewModel(get(), get()) }
    viewModel { ReminderViewModel(get()) }
    viewModel { TimelineViewModel(get(), get(), get(), get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { FamilyViewModel(get(), get(), get(), get()) }
    viewModel { AiChatViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AiSettingsViewModel(get(), get(), get()) }
    single { AiContextBuilder(get(), get(), get(), get(), get()) }
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
    single { get<AppDatabase>().syncCursorDao() }
}

// ── Supabase 同步模块 ──
val syncModule = module {
    single { com.babytracker.core.sync.SupabaseProvider.client }
    single { com.babytracker.core.auth.AuthService(get(), get()) }
    single { FamilyService(get(), get()) }
    single { com.babytracker.core.sync.SyncEngine(get(), get()) }
    single { com.babytracker.core.sync.RealtimeManager(get(), get(), get()) }
    single { SyncTrigger(get(), get(), get(), get(), get(), get(), get(), androidContext()) }
    single {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    single { AiConfigStore(get()) }
    single { AiDeviceKeyStore() }
    single { AiBootstrapClient(get(), get()) }
    single { AiConfigCoordinator(get(), get(), get(), get(), get()) }
    single { OpenAiCompatibleChatAdapter(get()) }
    single { OpenAiResponsesAdapter(get()) }
    single {
        AiProviderClient(
            adapters = listOf(get<OpenAiCompatibleChatAdapter>(), get<OpenAiResponsesAdapter>()),
            configCoordinator = get(),
            deviceKeyStore = get(),
        )
    }
}
