package com.peakvalle.hours.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peakvalle.hours.data.notification.Notifications
import com.peakvalle.hours.data.settings.SettingsRepository
import com.peakvalle.hours.domain.model.AppSettings
import com.peakvalle.hours.domain.model.SoundType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val app: Application
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setLeadMinutes(minutes: Int) {
        viewModelScope.launch { repository.setLeadMinutes(minutes) }
    }

    fun setSound(type: SoundType, uri: String = "") {
        viewModelScope.launch { repository.setSound(type, uri) }
    }

    fun setOngoingEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setOngoingEnabled(enabled) }
    }

    /** Publica una notificación de prueba con la configuración actual. */
    fun sendTest() {
        viewModelScope.launch {
            val settings = repository.settings.first()
            if (settings.notificationsEnabled && Notifications.areEnabled(app)) {
                Notifications.showTest(app, settings)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as Application
                SettingsViewModel(SettingsRepository(app), app)
            }
        }
    }
}