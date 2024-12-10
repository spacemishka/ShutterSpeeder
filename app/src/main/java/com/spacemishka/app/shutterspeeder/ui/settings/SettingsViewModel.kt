package com.spacemishka.app.shutterspeeder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spacemishka.app.shutterspeeder.data.AppPreferences
import com.spacemishka.app.shutterspeeder.data.DeviationThresholds
import com.spacemishka.app.shutterspeeder.serial.DeviceType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: AppPreferences
) : ViewModel() {
    val deviationThresholds: StateFlow<DeviationThresholds> = preferences.deviationThresholds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeviationThresholds()
        )

    val deviceType: StateFlow<DeviceType> = preferences.deviceType
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeviceType.STM32
        )

    fun updateThresholds(warning: Double?, error: Double?) {
        viewModelScope.launch {
            preferences.updateDeviationThresholds(warning, error)
        }
    }

    fun updateDeviceType(deviceType: DeviceType) {
        viewModelScope.launch {
            preferences.updateDeviceType(deviceType)
        }
    }

    class Factory(private val preferences: AppPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferences) as T
        }
    }
} 