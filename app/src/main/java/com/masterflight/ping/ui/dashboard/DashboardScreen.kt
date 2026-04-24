package com.masterflight.ping.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.masterflight.ping.R
import com.masterflight.ping.data.network.NetworkStatus
import com.masterflight.ping.ui.dashboard.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToPing: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    onNavigateToSubnet: () -> Unit,
    onNavigateToTrace: () -> Unit
) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.dashboard_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = stringResource(id = R.string.refresh)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusHeader(networkStatus)

                Spacer(modifier = Modifier.height(12.dp))

                // 全部改为垂直排列
                CompactDetailCard(
                    title = stringResource(id = R.string.connection_type_title),
                    value = networkStatus.connectionType,
                    icon = when (networkStatus.connectionType) {
                        context.getString(R.string.conn_wifi) -> Icons.Rounded.SignalWifi4Bar
                        context.getString(R.string.conn_cellular) -> Icons.Rounded.SignalCellularAlt
                        else -> Icons.Rounded.Dns
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CompactDetailCard(
                    title = stringResource(id = R.string.local_ip_title),
                    value = networkStatus.localIp,
                    icon = Icons.Rounded.Router
                )

                Spacer(modifier = Modifier.height(8.dp))

                CompactDetailCard(
                    title = stringResource(id = R.string.public_ip_title),
                    value = networkStatus.publicIp,
                    icon = Icons.Rounded.Public
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 功能按钮区域
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton(
                        text = stringResource(id = R.string.discover_devices),
                        icon = Icons.Rounded.Devices,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = onNavigateToDiscovery
                    )

                    ActionButton(
                        text = stringResource(id = R.string.subnet_scan_title),
                        icon = Icons.Rounded.Search,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onNavigateToSubnet
                    )

                    ActionButton(
                        text = stringResource(id = R.string.open_ping_tool),
                        icon = Icons.Rounded.NetworkCheck,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToPing
                    )

                    ActionButton(
                        text = stringResource(id = R.string.traceroute),
                        icon = Icons.Rounded.Router,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = onNavigateToTrace
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun StatusHeader(status: NetworkStatus) {
    val statusColor = if (status.isConnected) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.error

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.NetworkCheck,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = statusColor
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = if (status.isConnected) 
                stringResource(id = R.string.connected) 
            else 
                stringResource(id = R.string.no_connection),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}

@Composable
fun CompactDetailCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
