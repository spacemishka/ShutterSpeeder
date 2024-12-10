package com.spacemishka.app.shutterspeeder.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spacemishka.app.shutterspeeder.data.AppPreferences
import com.spacemishka.app.shutterspeeder.data.ShutterSpeedDatabase
import com.spacemishka.app.shutterspeeder.data.entity.Camera
import com.spacemishka.app.shutterspeeder.data.entity.Measurement
import com.spacemishka.app.shutterspeeder.serial.SerialService
import com.spacemishka.app.shutterspeeder.ui.measurement.MeasurementViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CameraDetailViewModel(
    private val database: ShutterSpeedDatabase,
    private val camera: Camera,
    serialService: SerialService,
    private val preferences: AppPreferences
) : ViewModel() {

    val measurements: StateFlow<List<Measurement>> = database.measurementDao()
        .getMeasurementsForCamera(camera.id)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val measurementViewModel = MeasurementViewModel(database, camera.id, serialService, preferences)

    class Factory(
        private val database: ShutterSpeedDatabase,
        private val camera: Camera,
        private val serialService: SerialService,
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CameraDetailViewModel(database, camera, serialService, preferences) as T
        }
    }
} 