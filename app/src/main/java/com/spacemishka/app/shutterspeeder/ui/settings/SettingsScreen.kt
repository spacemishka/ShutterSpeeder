package com.spacemishka.app.shutterspeeder.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.spacemishka.app.shutterspeeder.serial.DeviceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val thresholds by viewModel.deviationThresholds.collectAsState()
    val currentDeviceType by viewModel.deviceType.collectAsState()
    var warningThreshold by remember { mutableStateOf(thresholds.warning.toString()) }
    var errorThreshold by remember { mutableStateOf(thresholds.error.toString()) }
    var showError by remember { mutableStateOf(false) }
    var deviceTypeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Device Type",
                style = MaterialTheme.typography.titleMedium
            )

            Box {
                OutlinedButton(
                    onClick = { deviceTypeExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(currentDeviceType.displayName)
                }

                DropdownMenu(
                    expanded = deviceTypeExpanded,
                    onDismissRequest = { deviceTypeExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DeviceType.values().forEach { deviceType ->
                        DropdownMenuItem(
                            text = { Text(deviceType.displayName) },
                            onClick = {
                                viewModel.updateDeviceType(deviceType)
                                deviceTypeExpanded = false
                            }
                        )
                    }
                }
            }

            Divider()

            Text(
                "Measurement Thresholds",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = warningThreshold,
                onValueChange = { 
                    warningThreshold = it
                    showError = false
                },
                label = { Text("Warning Threshold (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = errorThreshold,
                onValueChange = { 
                    errorThreshold = it
                    showError = false
                },
                label = { Text("Error Threshold (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (showError) {
                Text(
                    "Error threshold must be greater than warning threshold",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    val warning = warningThreshold.toDoubleOrNull()
                    val error = errorThreshold.toDoubleOrNull()
                    if (warning != null && error != null && error > warning) {
                        viewModel.updateThresholds(warning, error)
                        showError = false
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
} 