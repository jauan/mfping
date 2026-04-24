package com.masterflight.ping.data.network

data class TraceStep(
    val hop: Int,
    val ip: String? = null,
    val hostname: String? = null,
    val responseTimeMs: Double? = null,
    val isTimeout: Boolean = false
)
