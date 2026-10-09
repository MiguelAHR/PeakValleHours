package com.peakvalle.hours.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.peakvalle.hours.domain.model.AppSettings
import com.peakvalle.hours.domain.model.SoundType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "peakvalle_settings")

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { throwable ->
            // Si el archivo está corrupto se usan los valores por defecto.
            if (throwable is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw throwable
        }
        .map { prefs ->
            AppSettings(
                notificationsEnabled = prefs[KEY_ENABLED] ?: true,
                leadMinutes = prefs[KEY_LEAD] ?: 0,
                soundType = prefs[KEY_SOUND_TYPE]
                    ?.let { name -> SoundType.entries.firstOrNull { it.name == name } }
                    ?: SoundType.DEFAULT,
                soundUri = prefs[KEY_SOUND_URI] ?: ""
            )
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ENABLED] = enabled }
    }

    suspend fun setLeadMinutes(minutes: Int) {
        context.dataStore.edit { it[KEY_LEAD] = minutes }
    }

    suspend fun setSound(type: SoundType, uri: String) {
        context.dataStore.edit {
            it[KEY_SOUND_TYPE] = type.name
            it[KEY_SOUND_URI] = uri
        }
    }

    private companion object {
        val KEY_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_LEAD = intPreferencesKey("lead_minutes")
        val KEY_SOUND_TYPE = stringPreferencesKey("sound_type")
        val KEY_SOUND_URI = stringPreferencesKey("sound_uri")
    }
}