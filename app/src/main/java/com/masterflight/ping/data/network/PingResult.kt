package com.masterflight.ping.data.network

sealed class PingResult {
    data class Success(val latency: Double, val seq: Int, val ttl: Int) : PingResult()
    data class Error(val message: String) : PingResult()
    data object Loading : PingResult()
}

data class PingStats(
    val minRtt: Double = Double.MAX_VALUE,
    val maxRtt: Double = 0.0,
    val avgRtt: Double = 0.0,
    val count: Int = 0,
    val totalRtt: Double = 0.0,
    val packetLoss: Double = 0.0,
    val packetsSent: Int = 0,
    val packetsReceived: Int = 0
) {
    val displayMin: Double get() = if (count == 0) 0.0 else minRtt
}
