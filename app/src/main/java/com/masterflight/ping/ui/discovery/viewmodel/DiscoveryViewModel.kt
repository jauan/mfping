package com.masterflight.ping.ui.discovery.viewmodel

import android.util.Log
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

    private val TAG = "DiscoveryVM"

    private val _scanState = MutableStateFlow(SubnetScanState())
    val scanState: StateFlow<SubnetScanState> = _scanState.asStateFlow()

    private val _devices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val devices: StateFlow<List<DiscoveredDevice>> = _devices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    fun startDiscovery(localIp: String) {
        Log.d(TAG, "startDiscovery: $localIp")
        viewModelScope.launch {
            _isDiscovering.value = true
            try {
                repository.discoverDevices(localIp).collect { discovered ->
                    _devices.value = discovered
                }
            } catch (e: Exception) {
                Log.e(TAG, "Discovery error", e)
            } finally {
                _isDiscovering.value = false
            }
        }
    }

    fun startSubnetScan(inputPrefix: String) {
        Log.d(TAG, "startSubnetScan input: $inputPrefix")
        
        // 智能前缀提取逻辑
        val prefix = if (inputPrefix.count { it == '.' } >= 3) {
            inputPrefix.substringBeforeLast(".")
        } else {
            inputPrefix.trimEnd('.')
        }
        
        Log.d(TAG, "startSubnetScan resolved prefix: $prefix")
        
        if (prefix.isEmpty()) {
            Log.w(TAG, "Prefix is empty, skipping scan")
            return
        }

        viewModelScope.launch {
            _scanState.value = _scanState.value.copy(isScanning = true, currentIpPrefix = prefix)
            try {
                repository.scanSubnet(prefix).collect { results ->
                    _scanState.value = _scanState.value.copy(results = results)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Subnet scan error", e)
            } finally {
                _scanState.value = _scanState.value.copy(isScanning = false)
            }
        }
    }
}
