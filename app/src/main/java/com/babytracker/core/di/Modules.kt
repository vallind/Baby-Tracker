package com.babytracker.core.di

import android.content.Context
import android.content.SharedPreferences
import com.babytracker.core.backup.BackupManager
import com.babytracker.core.database.AppDatabase
import com.babytracker.core.theme.ThemeController
import com.babytracker.core.util.BabyController
import com.babytracker.data.repository.*
import com.babytracker.ui.stats.StatsViewModel
import com.babytracker.ui.home.HomeViewModel
import com.babytracker.ui.message.MessageViewModel
import com.babytracker.ui.development.DevelopmentAssessmentViewModel
import com.babytracker.ui.reminder.ReminderViewModel
import com.babytracker.ui.timeline.TimelineViewModel
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
    viewModel { TimelineViewModel(get(), get(), get(), get(), get(), get()) }
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
}
