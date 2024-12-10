package com.spacemishka.app.shutterspeeder

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.spacemishka.app.shutterspeeder.data.ShutterSpeedDatabase
import com.spacemishka.app.shutterspeeder.serial.SerialService
import com.spacemishka.app.shutterspeeder.ui.home.HomeScreen
import com.spacemishka.app.shutterspeeder.ui.home.HomeViewModel
import com.spacemishka.app.shutterspeeder.ui.measurement.MeasurementDialog
import com.spacemishka.app.shutterspeeder.ui.measurement.MeasurementViewModel
import com.spacemishka.app.shutterspeeder.ui.theme.ShutterSpeederTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.spacemishka.app.shutterspeeder.data.entity.Camera
import com.spacemishka.app.shutterspeeder.ui.detail.CameraDetailScreen
import com.spacemishka.app.shutterspeeder.ui.detail.CameraDetailViewModel
import com.spacemishka.app.shutterspeeder.data.AppPreferences
import com.spacemishka.app.shutterspeeder.ui.settings.SettingsScreen
import com.spacemishka.app.shutterspeeder.ui.settings.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(ShutterSpeedDatabase.getDatabase(applicationContext))
    }
    
    private lateinit var serialService: SerialService
    private lateinit var appPreferences: AppPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize preferences first
        appPreferences = AppPreferences(applicationContext)
        
        // Then initialize SerialService with preferences
        serialService = SerialService(
            getSystemService(Context.USB_SERVICE) as UsbManager,
            appPreferences
        )
        
        enableEdgeToEdge()
        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
            var selectedCamera by remember { mutableStateOf<Camera?>(null) }
            
            ShutterSpeederTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Home -> {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onCameraClick = { camera -> 
                                    selectedCamera = camera
                                    currentScreen = Screen.Detail
                                },
                                onSettingsClick = { currentScreen = Screen.Settings }
                            )
                        }
                        Screen.Settings -> {
                            val settingsViewModel: SettingsViewModel by viewModels {
                                SettingsViewModel.Factory(appPreferences)
                            }
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                        }
                        Screen.Detail -> {
                            selectedCamera?.let { camera ->
                                val detailViewModel: CameraDetailViewModel by viewModels {
                                    CameraDetailViewModel.Factory(
                                        database = ShutterSpeedDatabase.getDatabase(applicationContext),
                                        camera = camera,
                                        serialService = serialService,
                                        preferences = appPreferences
                                    )
                                }
                                CameraDetailScreen(
                                    camera = camera,
                                    viewModel = detailViewModel,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen {
    data object Home : Screen()
    data object Settings : Screen()
    data object Detail : Screen()
}