package com.masterflight.ping.data.network

data class PortStatus(
    val port: Int,
    val isOpen: Boolean,
    val serviceName: String? = null
)
