package com.spacemishka.app.shutterspeeder.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacemishka.app.shutterspeeder.data.entity.Camera

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCameraClick: (Camera) -> Unit,
    onSettingsClick: () -> Unit
) {
    val cameras by viewModel.cameras.collectAsState(initial = emptyList())
    val showDialog by viewModel.showAddDialog.collectAsState()

    if (showDialog) {
        AddCameraDialog(
            onDismiss = { viewModel.hideAddCameraDialog() },
            onConfirm = { manufacturer, model, serialNumber ->
                viewModel.addCamera(manufacturer, model, serialNumber)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShutterSpeeder") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddCameraDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new camera")
            }
        }
    ) { padding ->
        if (cameras.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            CameraList(
                cameras = cameras,
                onCameraClick = onCameraClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun CameraList(
    cameras: List<Camera>,
    onCameraClick: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cameras) { camera ->
            CameraItem(
                camera = camera,
                onClick = { onCameraClick(camera) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CameraItem(
    camera: Camera,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "${camera.manufacturer} ${camera.model}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "S/N: ${camera.serialNumber}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No cameras added yet",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Click + to add a new camera",
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 