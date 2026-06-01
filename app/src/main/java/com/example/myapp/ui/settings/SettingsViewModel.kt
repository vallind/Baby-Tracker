package com.example.myapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.datastore.NotificationPreference
import com.example.myapp.data.datastore.ThemePreference
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsEvent {
    data class SetVaccineReminder(val enabled: Boolean) : SettingsEvent
}

class SettingsViewModel(
    private val themePref: ThemePreference,
    private val notificationPref: NotificationPreference
) : ViewModel() {
    val themeMode: StateFlow<Int> = themePref.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val vaccineReminder: StateFlow<Boolean> = notificationPref.vaccineReminder.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetVaccineReminder -> viewModelScope.launch { notificationPref.setVaccineReminder(event.enabled) }
        }
    }
}
