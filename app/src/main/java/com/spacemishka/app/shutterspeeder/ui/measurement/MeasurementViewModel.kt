package com.spacemishka.app.shutterspeeder.ui.measurement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spacemishka.app.shutterspeeder.data.ShutterSpeed
import com.spacemishka.app.shutterspeeder.data.ShutterSpeedDatabase
import com.spacemishka.app.shutterspeeder.data.entity.Measurement
import com.spacemishka.app.shutterspeeder.data.AppPreferences
import com.spacemishka.app.shutterspeeder.serial.SerialService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class MeasurementState {
    object Idle : MeasurementState()
    object Measuring : MeasurementState()
    data class Success(val measurement: Measurement) : MeasurementState()
    data class Error(val message: String) : MeasurementState()
}

sealed class SerialStatus {
    object Disconnected : SerialStatus()
    object Connected : SerialStatus()
    data class Error(val message: String) : SerialStatus()
}

class MeasurementViewModel(
    private val database: ShutterSpeedDatabase,
    private val cameraId: Long,
    private val serialService: SerialService,
    private val preferences: AppPreferences
) : ViewModel() {
    private val _measurementState = MutableStateFlow<MeasurementState>(MeasurementState.Idle)
    val measurementState: StateFlow<MeasurementState> = _measurementState.asStateFlow()

    private val _serialStatus = MutableStateFlow<SerialStatus>(SerialStatus.Disconnected)
    val serialStatus: StateFlow<SerialStatus> = _serialStatus.asStateFlow()

    val deviationThresholds = preferences.deviationThresholds

    fun updateDeviationThresholds(warning: Double?, error: Double?) {
        viewModelScope.launch {
            preferences.updateDeviationThresholds(warning, error)
        }
    }

    fun connectDevice() {
        viewModelScope.launch {
            try {
                serialService.connect()
                    .onSuccess { _serialStatus.value = SerialStatus.Connected }
                    .onFailure { e -> 
                        _serialStatus.value = SerialStatus.Error(e.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                _serialStatus.value = SerialStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun startMeasurement(shutterSpeed: ShutterSpeed, selectedShutterSpeed: String) {
        viewModelScope.launch {
            _measurementState.value = MeasurementState.Idle
            delay(100)
            _measurementState.value = MeasurementState.Measuring
            
            try {
                serialService.measure()
                    .collect { measurementData ->
                        val measurement = measurementData.toMeasurement(
                            cameraId = cameraId,
                            referenceShutterSpeed = shutterSpeed.speed,
                            referenceSpeedMicros = shutterSpeed.microseconds,
                            selectedShutterSpeed = selectedShutterSpeed
                        )
                        saveMeasurement(measurement)
                    }
            } catch (e: Exception) {
                _measurementState.value = MeasurementState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _measurementState.value = MeasurementState.Idle
        serialService.reset()
    }

    override fun onCleared() {
        super.onCleared()
        serialService.disconnect()
    }

    fun saveMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            try {
                val id = database.measurementDao().insert(measurement)
                val savedMeasurement = measurement.copy(id = id)
                _measurementState.value = MeasurementState.Success(savedMeasurement)
            } catch (e: Exception) {
                _measurementState.value = MeasurementState.Error(e.message ?: "Failed to save measurement")
            }
        }
    }

    class Factory(
        private val database: ShutterSpeedDatabase,
        private val cameraId: Long,
        private val serialService: SerialService,
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MeasurementViewModel(database, cameraId, serialService, preferences) as T
        }
    }
} 