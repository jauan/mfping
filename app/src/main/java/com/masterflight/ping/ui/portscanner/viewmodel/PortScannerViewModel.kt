package com.masterflight.ping.ui.portscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterflight.ping.data.network.PortRepository
import com.masterflight.ping.data.network.PortStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PortScannerViewModel(private val repository: PortRepository) : ViewModel() {

    private val _ports = MutableStateFlow<List<PortStatus>>(emptyList())
    val ports: StateFlow<List<PortStatus>> = _ports.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun startScan(ip: String, customPorts: String = "") {
        val portList = if (customPorts.isBlank()) {
            repository.getCommonPorts()
        } else {
            customPorts.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..65535 }
        }

        if (portList.isEmpty()) return

        viewModelScope.launch {
            _isScanning.value = true
            _ports.value = emptyList()
            repository.scanPorts(ip, portList).collect { results ->
                _ports.value = results
                _isScanning.value = false
            }
        }
    }
}
