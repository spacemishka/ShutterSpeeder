package com.spacemishka.app.shutterspeeder.ui.measurement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spacemishka.app.shutterspeeder.data.DeviationThresholds
import com.spacemishka.app.shutterspeeder.data.ShutterSpeed
import com.spacemishka.app.shutterspeeder.data.entity.Camera
import kotlin.math.absoluteValue

@Composable
fun MeasurementDialog(
    camera: Camera,
    viewModel: MeasurementViewModel,
    onDismiss: () -> Unit
) {
    // Reset state when dialog opens
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    val measurementState by viewModel.measurementState.collectAsState()
    val serialStatus by viewModel.serialStatus.collectAsState()
    var selectedShutterSpeed by remember { mutableStateOf<ShutterSpeed?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showThresholdSettings by remember { mutableStateOf(false) }
    val thresholds by viewModel.deviationThresholds.collectAsState(initial = DeviationThresholds())

    if (showThresholdSettings) {
        ThresholdSettingsDialog(
            currentThresholds = thresholds,
            onSave = { warning, error -> 
                viewModel.updateDeviationThresholds(warning, error)
                showThresholdSettings = false
            },
            onDismiss = { showThresholdSettings = false }
        )
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.resetState()
            onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New Measurement")
                IconButton(onClick = { showThresholdSettings = true }) {
                    Icon(Icons.Default.Settings, "Threshold Settings")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Camera: ${camera.manufacturer} ${camera.model}")
                Text("S/N: ${camera.serialNumber}")

                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Shutter Speed Selection using DropDownMenu
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedShutterSpeed?.toString() ?: "Select Shutter Speed")
                    }
                    
                    androidx.compose.material.DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        ShutterSpeed.values().forEach { speed ->
                            androidx.compose.material.DropdownMenuItem(
                                onClick = {
                                    selectedShutterSpeed = speed
                                    expanded = false
                                }
                            ) {
                                Text(speed.toString())
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                when (serialStatus) {
                    is SerialStatus.Disconnected -> {
                        Text("USB Device not connected")
                        Button(
                            onClick = { viewModel.connectDevice() },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Connect Device")
                        }
                    }
                    is SerialStatus.Connected -> {
                        when (measurementState) {
                            is MeasurementState.Idle -> {
                                Button(
                                    onClick = { 
                                        selectedShutterSpeed?.let { speed ->
                                            viewModel.startMeasurement(speed, speed.speed)
                                        }
                                    },
                                    enabled = selectedShutterSpeed != null,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Start Measurement")
                                }
                            }
                            is MeasurementState.Measuring -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    "Measuring...",
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            is MeasurementState.Success -> {
                                val measurement = (measurementState as MeasurementState.Success).measurement
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Measurement completed",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    
                                    // Reference and selected speeds
                                    Text(
                                        "Reference: ${measurement.referenceShutterSpeed} (${measurement.referenceSpeedMicros}μs)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Selected: ${measurement.selectedShutterSpeed}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    // Measured speeds with deviation
                                    MeasuredSpeedRow(
                                        label = "Bottom Left",
                                        duration = measurement.bottomLeftDuration,
                                        deviation = measurement.bottomLeftDeviationPercent,
                                        reference = measurement.referenceSpeedMicros,
                                        warningThreshold = thresholds.warning,
                                        errorThreshold = thresholds.error
                                    )
                                    
                                    MeasuredSpeedRow(
                                        label = "Center",
                                        duration = measurement.centerDuration,
                                        deviation = measurement.centerDeviationPercent,
                                        reference = measurement.referenceSpeedMicros,
                                        warningThreshold = thresholds.warning,
                                        errorThreshold = thresholds.error
                                    )
                                    
                                    MeasuredSpeedRow(
                                        label = "Top Right",
                                        duration = measurement.topRightDuration,
                                        deviation = measurement.topRightDeviationPercent,
                                        reference = measurement.referenceSpeedMicros,
                                        warningThreshold = thresholds.warning,
                                        errorThreshold = thresholds.error
                                    )
                                }
                            }
                            is MeasurementState.Error -> {
                                Text(
                                    (measurementState as MeasurementState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                    is SerialStatus.Error -> {
                        Text(
                            (serialStatus as SerialStatus.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                enabled = measurementState !is MeasurementState.Measuring
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ThresholdSettingsDialog(
    currentThresholds: DeviationThresholds,
    onSave: (warning: Double?, error: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var warningThreshold by remember { mutableStateOf(currentThresholds.warning.toString()) }
    var errorThreshold by remember { mutableStateOf(currentThresholds.error.toString()) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deviation Thresholds") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = warningThreshold,
                    onValueChange = { warningThreshold = it },
                    label = { Text("Warning Threshold (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = errorThreshold,
                    onValueChange = { errorThreshold = it },
                    label = { Text("Error Threshold (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (showError) {
                    Text(
                        "Error threshold must be greater than warning threshold",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val warning = warningThreshold.toDoubleOrNull()
                    val error = errorThreshold.toDoubleOrNull()
                    if (warning != null && error != null && error > warning) {
                        onSave(warning, error)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MeasuredSpeedRow(
    label: String,
    duration: Long,
    deviation: Double,
    reference: Long,
    warningThreshold: Double,
    errorThreshold: Double
) {
    val deviationColor = when {
        deviation.absoluteValue <= warningThreshold -> MaterialTheme.colorScheme.primary
        deviation.absoluteValue <= errorThreshold -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${duration}μs",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${String.format("%.1f", deviation)}%",
            style = MaterialTheme.typography.bodyMedium,
            color = deviationColor
        )
    }
} 