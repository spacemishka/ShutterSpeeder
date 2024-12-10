package com.spacemishka.app.shutterspeeder.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spacemishka.app.shutterspeeder.data.entity.Camera
import com.spacemishka.app.shutterspeeder.data.entity.Measurement
import com.spacemishka.app.shutterspeeder.ui.measurement.MeasurementDialog
import kotlin.math.absoluteValue
import kotlin.text.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailScreen(
    camera: Camera,
    viewModel: CameraDetailViewModel,
    onBack: () -> Unit
) {
    val measurements by viewModel.measurements.collectAsState()
    var showMeasurementDialog by remember { mutableStateOf(false) }

    if (showMeasurementDialog) {
        MeasurementDialog(
            camera = camera,
            viewModel = viewModel.measurementViewModel,
            onDismiss = { showMeasurementDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${camera.manufacturer} ${camera.model}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showMeasurementDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Measurement")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Serial Number: ${camera.serialNumber}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (measurements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No measurements yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(measurements) { measurement ->
                        MeasurementItem(measurement = measurement)
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementItem(measurement: Measurement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with shutter speeds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Selected: ${measurement.selectedShutterSpeed}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Reference: ${measurement.referenceShutterSpeed} (${measurement.referenceSpeedMicros}μs)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = formatTimestamp(measurement.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Left Sensor
            SensorMeasurement(
                title = "Bottom Left Sensor",
                duration = measurement.bottomLeftDuration,
                deviation = measurement.bottomLeftDeviation,
                deviationPercent = measurement.bottomLeftDeviationPercent
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Center Sensor
            SensorMeasurement(
                title = "Center Sensor",
                duration = measurement.centerDuration,
                deviation = measurement.centerDeviation,
                deviationPercent = measurement.centerDeviationPercent
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Top Right Sensor
            SensorMeasurement(
                title = "Top Right Sensor",
                duration = measurement.topRightDuration,
                deviation = measurement.topRightDeviation,
                deviationPercent = measurement.topRightDeviationPercent
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Firmware: ${measurement.firmwareVersion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SensorMeasurement(
    title: String,
    duration: Long,
    deviation: Long,
    deviationPercent: Double
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Duration: ${duration}μs",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = String.format("%+.1f%%", deviationPercent),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    deviationPercent.absoluteValue <= 5.0 -> MaterialTheme.colorScheme.primary
                    deviationPercent.absoluteValue <= 10.0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }
        Text(
            text = "Deviation: ${if (deviation >= 0) "+" else ""}${deviation}μs",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    // TODO: Implement proper date formatting
    return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
} 