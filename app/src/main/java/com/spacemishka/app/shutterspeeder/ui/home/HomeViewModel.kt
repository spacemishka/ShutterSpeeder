package com.spacemishka.app.shutterspeeder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spacemishka.app.shutterspeeder.data.ShutterSpeedDatabase
import com.spacemishka.app.shutterspeeder.data.entity.Camera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val database: ShutterSpeedDatabase
) : ViewModel() {
    val cameras: StateFlow<List<Camera>> = database.cameraDao()
        .getAllCameras()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    fun showAddCameraDialog() {
        _showAddDialog.value = true
    }

    fun hideAddCameraDialog() {
        _showAddDialog.value = false
    }

    fun addCamera(manufacturer: String, model: String, serialNumber: String) {
        viewModelScope.launch {
            val camera = Camera(
                manufacturer = manufacturer,
                model = model,
                serialNumber = serialNumber
            )
            database.cameraDao().insertCamera(camera)
            hideAddCameraDialog()
        }
    }

    class Factory(private val database: ShutterSpeedDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(database) as T
        }
    }
} 