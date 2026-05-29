package com.example.edutrack.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val REG_NUM_KEY = stringPreferencesKey("registration_number")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        val ALARM_DAYS_KEY = androidx.datastore.preferences.core.intPreferencesKey("alarm_days")
        val ALARM_HOURS_KEY = androidx.datastore.preferences.core.intPreferencesKey("alarm_hours")
        val ALARM_MINUTES_KEY = androidx.datastore.preferences.core.intPreferencesKey("alarm_minutes")
    }

    val alarmDays: Flow<Int> = context.dataStore.data.map { it[ALARM_DAYS_KEY] ?: 0 }
    val alarmHours: Flow<Int> = context.dataStore.data.map { it[ALARM_HOURS_KEY] ?: 0 }
    val alarmMinutes: Flow<Int> = context.dataStore.data.map { it[ALARM_MINUTES_KEY] ?: 0 }

    suspend fun setAlarmSettings(days: Int, hours: Int, minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_DAYS_KEY] = days
            preferences[ALARM_HOURS_KEY] = hours
            preferences[ALARM_MINUTES_KEY] = minutes
        }
    }

    val registrationNumber: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REG_NUM_KEY]
    }

    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }

    suspend fun setUserSession(regNum: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[REG_NUM_KEY] = regNum
            preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun setRegistrationNumber(regNum: String) {
        context.dataStore.edit { preferences ->
            preferences[REG_NUM_KEY] = regNum
        }
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
}
