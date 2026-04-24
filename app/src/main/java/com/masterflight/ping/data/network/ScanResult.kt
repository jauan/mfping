package com.masterflight.ping.data.network

sealed class ScanStatus {
    object Idle : ScanStatus()
    object Scanning : ScanStatus()
    object Online : ScanStatus()
    object Offline : ScanStatus()
}

data class SubnetScanState(
    val results: Map<Int, ScanStatus> = emptyMap(),
    val isScanning: Boolean = false,
    val currentIpPrefix: String = ""
)
