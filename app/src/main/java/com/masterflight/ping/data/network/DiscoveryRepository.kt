package com.masterflight.ping.data.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class DiscoveryRepository(private val context: Context) {

    private val TAG = "DiscoveryRepo"

    fun discoverDevices(localIp: String): Flow<List<DiscoveredDevice>> = flow {
        if (localIp == "Unknown" || localIp.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val prefix = localIp.substringBeforeLast(".")
        coroutineScope {
            val results = (1..254).map { i ->
                async(Dispatchers.IO) {
                    val testIp = "$prefix.$i"
                    if (testIp == localIp) return@async null
                    
                    val reachable = isReallyReachable(testIp)
                    if (reachable) {
                        try {
                            val address = InetAddress.getByName(testIp)
                            DiscoveredDevice(
                                ip = testIp,
                                hostname = address.canonicalHostName,
                                isReachable = true
                            )
                        } catch (e: Exception) {
                            DiscoveredDevice(ip = testIp, isReachable = true)
                        }
                    } else null
                }
            }.awaitAll().filterNotNull()
            emit(results)
        }
    }.flowOn(Dispatchers.IO)

    fun scanSubnet(prefix: String): Flow<Map<Int, ScanStatus>> = flow {
        val results = mutableMapOf<Int, ScanStatus>()
        (1..254).forEach { results[it] = ScanStatus.Idle }
        emit(results.toMap())

        coroutineScope {
            val chunkSize = 5
            for (chunk in (1..254).chunked(chunkSize)) {
                chunk.map { i ->
                    async(Dispatchers.IO) {
                        val cleanPrefix = prefix.trimEnd('.')
                        val testIp = "$cleanPrefix.$i"
                        synchronized(results) { results[i] = ScanStatus.Scanning }
                        
                        val isOnline = isReallyReachable(testIp)
                        
                        synchronized(results) {
                            results[i] = if (isOnline) ScanStatus.Online else ScanStatus.Offline
                        }
                    }
                }.awaitAll()
                emit(results.toMap())
                delay(20)
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun isReallyReachable(ip: String): Boolean = withContext(Dispatchers.IO) {
        // 尝试 1: 原生 InetAddress.isReachable
        try {
            val address = InetAddress.getByName(ip)
            if (address.isReachable(1200)) return@withContext true
        } catch (e: Exception) { }

        // 尝试 2: 常用端口探测 (TCP SYN)
        val ports = listOf(80, 443, 445, 135, 22, 1, 5357)
        for (port in ports) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), 300)
                    return@withContext true
                }
            } catch (e: Exception) { }
        }

        // 尝试 3: 特殊处理网关 (.1)
        if (ip.endsWith(".1")) {
            try {
                val gatewayPorts = listOf(1900, 8080, 53)
                for (port in gatewayPorts) {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(ip, port), 400)
                        return@withContext true
                    }
                }
            } catch (e: Exception) {}
        }

        false
    }
}
