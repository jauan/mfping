package com.masterflight.ping.ui.portscanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.masterflight.ping.R
import com.masterflight.ping.data.network.PortStatus
import com.masterflight.ping.ui.portscanner.viewmodel.PortScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortScannerScreen(
    viewModel: PortScannerViewModel,
    ip: String,
    onBack: () -> Unit
) {
    val ports by viewModel.ports.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    var customPorts by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${stringResource(id = R.string.port_scan_title)}: $ip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = customPorts,
                onValueChange = { customPorts = it },
                label = { Text(stringResource(id = R.string.custom_ports_label)) },
                placeholder = { Text(stringResource(id = R.string.custom_ports_hint)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.startScan(ip, customPorts) },
                        enabled = !isScanning
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(ports) { portStatus ->
                    PortItem(portStatus)
                }
            }
        }
    }
}

@Composable
fun PortItem(portStatus: PortStatus) {
    ListItem(
        headlineContent = { 
            Text(
                "Port ${portStatus.port}", 
                fontWeight = FontWeight.Bold 
            ) 
        },
        supportingContent = { 
            Text(portStatus.serviceName ?: stringResource(id = R.string.unknown_service)) 
        },
        trailingContent = {
            if (portStatus.isOpen) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Open", tint = Color.Green)
            } else {
                Icon(Icons.Rounded.Error, contentDescription = "Closed", tint = Color.Gray)
            }
        }
    )
    HorizontalDivider()
}
