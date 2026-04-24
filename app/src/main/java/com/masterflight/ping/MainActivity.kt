package com.masterflight.ping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masterflight.ping.data.network.DiscoveryRepository
import com.masterflight.ping.data.network.NetworkRepository
import com.masterflight.ping.data.network.PingRepository
import com.masterflight.ping.data.network.PortRepository
import com.masterflight.ping.data.network.TraceRepository
import com.masterflight.ping.ui.dashboard.DashboardScreen
import com.masterflight.ping.ui.dashboard.viewmodel.DashboardViewModel
import com.masterflight.ping.ui.discovery.DiscoveryScreen
import com.masterflight.ping.ui.discovery.SubnetScanScreen
import com.masterflight.ping.ui.discovery.viewmodel.DiscoveryViewModel
import com.masterflight.ping.ui.ping.PingScreen
import com.masterflight.ping.ui.ping.viewmodel.PingViewModel
import com.masterflight.ping.ui.portscanner.PortScannerScreen
import com.masterflight.ping.ui.portscanner.viewmodel.PortScannerViewModel
import com.masterflight.ping.ui.theme.MfpingTheme
import com.masterflight.ping.ui.traceroute.TraceScreen
import com.masterflight.ping.ui.traceroute.viewmodel.TraceViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MfpingTheme {
                MainNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val networkRepository = remember { NetworkRepository(context.applicationContext) }
    val pingRepository = remember { PingRepository(context.applicationContext) }
    val discoveryRepository = remember { DiscoveryRepository(context.applicationContext) }
    val portRepository = remember { PortRepository() }
    val traceRepository = remember { TraceRepository() }
    
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val scope = rememberCoroutineScope()

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                val dashboardViewModel: DashboardViewModel = viewModel {
                    DashboardViewModel(networkRepository)
                }
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToPing = {
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, "ping")
                        }
                    },
                    onNavigateToDiscovery = {
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, "discovery")
                        }
                    },
                    onNavigateToSubnet = {
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, "subnet")
                        }
                    },
                    onNavigateToTrace = {
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, "traceroute")
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val currentContent = navigator.currentDestination?.content
                when (currentContent) {
                    "ping" -> {
                        val pingViewModel: PingViewModel = viewModel {
                            PingViewModel(pingRepository)
                        }
                        PingScreen(
                            viewModel = pingViewModel,
                            onBack = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            }
                        )
                    }
                    "discovery" -> {
                        val discoveryViewModel: DiscoveryViewModel = viewModel {
                            DiscoveryViewModel(discoveryRepository)
                        }
                        val dashboardViewModel: DashboardViewModel = viewModel {
                            DashboardViewModel(networkRepository)
                        }
                        val networkStatus by dashboardViewModel.networkStatus.collectAsState()
                        
                        DiscoveryScreen(
                            viewModel = discoveryViewModel,
                            localIp = networkStatus.localIp,
                            onBack = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            },
                            onDeviceClick = { ip ->
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, "portscanner/$ip")
                                }
                            }
                        )
                    }
                    "subnet" -> {
                        val discoveryViewModel: DiscoveryViewModel = viewModel {
                            DiscoveryViewModel(discoveryRepository)
                        }
                        val dashboardViewModel: DashboardViewModel = viewModel {
                            DashboardViewModel(networkRepository)
                        }
                        val networkStatus by dashboardViewModel.networkStatus.collectAsState()

                        SubnetScanScreen(
                            viewModel = discoveryViewModel,
                            localIp = networkStatus.localIp,
                            onBack = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            },
                            onDeviceClick = { ip ->
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, "portscanner/$ip")
                                }
                            }
                        )
                    }
                    "traceroute" -> {
                        val traceViewModel: TraceViewModel = viewModel {
                            TraceViewModel(traceRepository)
                        }
                        TraceScreen(
                            viewModel = traceViewModel,
                            onBack = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            }
                        )
                    }
                    else -> {
                        val currentStr = currentContent as? String
                        if (currentStr?.startsWith("portscanner/") == true) {
                            val ip = currentStr.substringAfter("portscanner/")
                            val portViewModel: PortScannerViewModel = viewModel {
                                PortScannerViewModel(portRepository)
                            }
                            PortScannerScreen(
                                viewModel = portViewModel,
                                ip = ip,
                                onBack = {
                                    scope.launch {
                                        navigator.navigateBack()
                                    }
                                }
                            )
                        } else {
                            DetailPlaceholder()
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DetailPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.detail_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
