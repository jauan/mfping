package com.masterflight.ping.ui.ping.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterflight.ping.data.network.PingRepository
import com.masterflight.ping.data.network.PingResult
import com.masterflight.ping.data.network.PingStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PingViewModel(private val repository: PingRepository) : ViewModel() {

    var host by mutableStateOf("")
    var isPinging by mutableStateOf(false)
        private set

    val pingLogs = mutableStateListOf<PingResult>()
    var stats by mutableStateOf(PingStats())
        private set

    private var pingJob: Job? = null

    fun togglePing() {
        if (isPinging) {
            stopPing()
        } else {
            startPing()
        }
    }

    private fun startPing() {
        if (host.isBlank()) return
        
        isPinging = true
        pingLogs.clear()
        stats = PingStats(packetsSent = 1)
        
        pingJob = viewModelScope.launch {
            repository.startPinging(host).collectLatest { result ->
                pingLogs.add(0, result)
                updateStats(result)
            }
        }
    }

    fun stopPing() {
        pingJob?.cancel()
        isPinging = false
    }

    private fun updateStats(result: PingResult) {
        val currentStats = stats
        when (result) {
            is PingResult.Success -> {
                val newCount = currentStats.count + 1
                val newTotalRtt = currentStats.totalRtt + result.latency
                val newMin = if (result.latency < currentStats.minRtt) result.latency else currentStats.minRtt
                val newMax = if (result.latency > currentStats.maxRtt) result.latency else currentStats.maxRtt
                val newAvg = (newTotalRtt / newCount * 10.0).roundToInt() / 10.0
                
                stats = currentStats.copy(
                    minRtt = newMin,
                    maxRtt = newMax,
                    avgRtt = newAvg,
                    count = newCount,
                    totalRtt = newTotalRtt,
                    packetsReceived = currentStats.packetsReceived + 1,
                    packetsSent = currentStats.packetsSent + 1
                )
            }
            is PingResult.Error -> {
                stats = currentStats.copy(
                    packetsSent = currentStats.packetsSent + 1
                )
            }
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPing()
    }
}
