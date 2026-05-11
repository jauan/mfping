package com.masterflight.ping.ui.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterflight.ping.R
import com.masterflight.ping.data.network.ScanStatus
import com.masterflight.ping.ui.discovery.viewmodel.DiscoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubnetScanScreen(
    viewModel: DiscoveryViewModel,
    localIp: String,
    onBack: () -> Unit,
    onDeviceClick: (String) -> Unit
) {
    val scanState by viewModel.scanState.collectAsState()
    var ipPrefix by remember { mutableStateOf("") }

    LaunchedEffect(localIp) {
        if (ipPrefix.isEmpty() && localIp.contains(".")) {
            ipPrefix = localIp.substringBeforeLast(".")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.subnet_scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            // 传入用户在输入框中修改后的前缀 ipPrefix，而不是原始的 localIp
                            viewModel.startSubnetScan(ipPrefix) 
                        },
                        enabled = !scanState.isScanning
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Start")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(8.dp)) {
            OutlinedTextField(
                value = ipPrefix,
                onValueChange = { 
                    ipPrefix = it.filter { char -> char.isDigit() || char == '.' }
                },
                label = { Text("Network Prefix") },
                placeholder = { Text("e.g. 192.168.1") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                trailingIcon = { 
                    Button(
                        onClick = { viewModel.startSubnetScan(ipPrefix) },
                        enabled = !scanState.isScanning && ipPrefix.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 4.dp).height(40.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(stringResource(id = R.string.start))
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (scanState.isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(8), // 固定为 8 列，方便看到一行行处理的效果
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items((1..254).toList()) { octet ->
                    val status = scanState.results[octet] ?: ScanStatus.Idle
                    GridItem(
                        octet = octet,
                        status = status,
                        onClick = {
                            if (status is ScanStatus.Online) {
                                onDeviceClick("${scanState.currentIpPrefix}.$octet")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GridItem(octet: Int, status: ScanStatus, onClick: () -> Unit) {
    val backgroundColor = when (status) {
        is ScanStatus.Online -> Color(0xFF4CAF50) // Green
        is ScanStatus.Offline -> MaterialTheme.colorScheme.surfaceVariant
        is ScanStatus.Scanning -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = when (status) {
        is ScanStatus.Online -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        enabled = status is ScanStatus.Online,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = String.format("%03d", octet),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
