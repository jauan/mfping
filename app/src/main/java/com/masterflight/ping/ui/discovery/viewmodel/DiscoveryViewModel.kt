package com.masterflight.ping.ui.discovery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterflight.ping.data.network.DiscoveredDevice
import com.masterflight.ping.data.network.DiscoveryRepository
import com.masterflight.ping.data.network.ScanStatus
import com.masterflight.ping.data.network.SubnetScanState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiscoveryViewModel(private val repository: DiscoveryRepository) : ViewModel() {

    private val _scanState = MutableStateFlow(SubnetScanState())
    val scanState: StateFlow<SubnetScanState> = _scanState.asStateFlow()

    private val _devices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val devices: StateFlow<List<DiscoveredDevice>> = _devices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    fun startDiscovery(localIp: String) {
        viewModelScope.launch {
            _isDiscovering.value = true
            repository.discoverDevices(localIp).collect { discovered ->
                _devices.value = discovered
                _isDiscovering.value = false
            }
        }
    }

    fun startSubnetScan(inputPrefix: String) {
        // 如果输入的就是前缀（如 192.168.7），直接使用
        // 如果输入的是完整 IP，提取前缀
        val prefix = if (inputPrefix.count { it == '.' } >= 3) {
            inputPrefix.substringBeforeLast(".")
        } else {
            inputPrefix.trimEnd('.')
        }

        if (prefix.isEmpty()) return

        viewModelScope.launch {
            _scanState.value = _scanState.value.copy(isScanning = true, currentIpPrefix = prefix)
            repository.scanSubnet(prefix).collect { results ->
                _scanState.value = _scanState.value.copy(results = results)
            }
            _scanState.value = _scanState.value.copy(isScanning = false)
        }
    }
}
