package com.masterflight.ping.data.network

data class NetworkStatus(
    val localIp: String = "Unknown",
    val publicIp: String = "Loading...",
    val connectionType: String = "Unknown",
    val isConnected: Boolean = false
)
