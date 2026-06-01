package com.example.myapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

class NotificationPreference(private val context: Context) {
    companion object {
        private val VACCINE_REMINDER = booleanPreferencesKey("vaccine_reminder")
        private val CHECKUP_REMINDER = booleanPreferencesKey("checkup_reminder")
    }

    val vaccineReminder: Flow<Boolean> = context.notificationStore.data.map { it[VACCINE_REMINDER] ?: true }
    val checkupReminder: Flow<Boolean> = context.notificationStore.data.map { it[CHECKUP_REMINDER] ?: true }

    suspend fun setVaccineReminder(enabled: Boolean) { context.notificationStore.edit { it[VACCINE_REMINDER] = enabled } }
    suspend fun setCheckupReminder(enabled: Boolean) { context.notificationStore.edit { it[CHECKUP_REMINDER] = enabled } }
}
