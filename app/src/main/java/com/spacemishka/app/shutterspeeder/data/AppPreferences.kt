package com.spacemishka.app.shutterspeeder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spacemishka.app.shutterspeeder.serial.DeviceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {
    private object PreferencesKeys {
        val WARNING_THRESHOLD = doublePreferencesKey("warning_threshold")
        val ERROR_THRESHOLD = doublePreferencesKey("error_threshold")
        val DEVICE_TYPE = stringPreferencesKey("device_type")
    }

    val deviationThresholds: Flow<DeviationThresholds> = context.dataStore.data.map { preferences ->
        DeviationThresholds(
            warning = preferences[PreferencesKeys.WARNING_THRESHOLD] ?: 5.0,
            error = preferences[PreferencesKeys.ERROR_THRESHOLD] ?: 10.0
        )
    }

    val deviceType: Flow<DeviceType> = context.dataStore.data.map { preferences ->
        val deviceTypeName = preferences[PreferencesKeys.DEVICE_TYPE] ?: DeviceType.STM32.displayName
        DeviceType.fromDisplayName(deviceTypeName) ?: DeviceType.STM32
    }

    suspend fun updateDeviationThresholds(warning: Double?, error: Double?) {
        context.dataStore.edit { preferences ->
            warning?.let { preferences[PreferencesKeys.WARNING_THRESHOLD] = it }
            error?.let { preferences[PreferencesKeys.ERROR_THRESHOLD] = it }
        }
    }

    suspend fun updateDeviceType(deviceType: DeviceType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_TYPE] = deviceType.displayName
        }
    }
} 