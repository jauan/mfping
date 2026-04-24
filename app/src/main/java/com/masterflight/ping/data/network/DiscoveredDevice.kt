package com.masterflight.ping.data.network

data class DiscoveredDevice(
    val ip: String,
    val hostname: String? = null,
    val mac: String? = null,
    val vendor: String? = null,
    val isReachable: Boolean = false
)
