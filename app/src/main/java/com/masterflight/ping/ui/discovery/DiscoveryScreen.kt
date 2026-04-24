package com.masterflight.ping.ui.discovery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.masterflight.ping.R
import com.masterflight.ping.data.network.DiscoveredDevice
import com.masterflight.ping.ui.discovery.viewmodel.DiscoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    localIp: String,
    onBack: () -> Unit,
    onDeviceClick: (String) -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val isDiscovering by viewModel.isDiscovering.collectAsState()

    LaunchedEffect(localIp) {
        if (devices.isEmpty() && !isDiscovering) {
            viewModel.startDiscovery(localIp)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.device_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isDiscovering) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (devices.isEmpty() && !isDiscovering) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.no_devices_found))
                }
            } else {
                LazyColumn {
                    items(devices) { device ->
                        DeviceItem(device, onDeviceClick)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(device: DiscoveredDevice, onClick: (String) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick(device.ip) },
        headlineContent = { Text(device.ip, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(device.hostname ?: stringResource(id = R.string.unknown_device)) },
        leadingContent = {
            Icon(Icons.Rounded.Devices, contentDescription = null)
        }
    )
    HorizontalDivider()
}
