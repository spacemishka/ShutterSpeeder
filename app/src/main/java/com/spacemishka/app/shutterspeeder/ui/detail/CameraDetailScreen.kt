package com.spacemishka.app.shutterspeeder.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spacemishka.app.shutterspeeder.data.entity.Camera
import com.spacemishka.app.shutterspeeder.data.entity.Measurement
import com.spacemishka.app.shutterspeeder.ui.measurement.MeasurementDialog
import kotlin.math.absoluteValue

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
            // Header with shutter speeds and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Selected Speed",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = measurement.selectedShutterSpeed,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatTimestamp(measurement.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Firmware: ${measurement.firmwareVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reference: ${measurement.referenceShutterSpeed} (${measurement.referenceSpeedMicros}μs)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Sensor measurements in a grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bottom Left Sensor
                SensorMeasurement(
                    title = "Bottom Left",
                    duration = measurement.bottomLeftDuration,
                    deviation = measurement.bottomLeftDeviation,
                    deviationPercent = measurement.bottomLeftDeviationPercent,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(120.dp)
                        .padding(horizontal = 8.dp)
                )

                // Center Sensor
                SensorMeasurement(
                    title = "Center",
                    duration = measurement.centerDuration,
                    deviation = measurement.centerDeviation,
                    deviationPercent = measurement.centerDeviationPercent,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(120.dp)
                        .padding(horizontal = 8.dp)
                )

                // Top Right Sensor
                SensorMeasurement(
                    title = "Top Right",
                    duration = measurement.topRightDuration,
                    deviation = measurement.topRightDeviation,
                    deviationPercent = measurement.topRightDeviationPercent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SensorMeasurement(
    title: String,
    duration: Long,
    deviation: Long,
    deviationPercent: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Duration
        Text(
            text = "${duration}μs",
            style = MaterialTheme.typography.bodyLarge
        )

        // Deviation percentage with color coding
        Text(
            text = String.format("%+.1f%%", deviationPercent),
            style = MaterialTheme.typography.titleMedium,
            color = when {
                deviationPercent.absoluteValue <= 5.0 -> MaterialTheme.colorScheme.primary
                deviationPercent.absoluteValue <= 10.0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.error
            }
        )

        // Absolute deviation
        Text(
            text = "${if (deviation >= 0) "+" else ""}${deviation}μs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return java.text.SimpleDateFormat("MMM d, yyyy HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
} 