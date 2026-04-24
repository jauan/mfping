package com.masterflight.ping.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetSocketAddress
import java.net.Socket

class PortRepository {

    private val commonPorts = mapOf(
        21 to "FTP",
        22 to "SSH",
        23 to "Telnet",
        25 to "SMTP",
        53 to "DNS",
        80 to "HTTP",
        110 to "POP3",
        143 to "IMAP",
        443 to "HTTPS",
        445 to "SMB",
        3306 to "MySQL",
        3389 to "RDP",
        5432 to "PostgreSQL",
        8080 to "HTTP-Proxy"
    )

    fun getCommonPorts(): List<Int> = commonPorts.keys.toList().sorted()

    fun scanPorts(ip: String, ports: List<Int>): Flow<List<PortStatus>> = flow {
        coroutineScope {
            val results = ports.map { port ->
                async(Dispatchers.IO) {
                    val isOpen = isPortOpen(ip, port)
                    PortStatus(port, isOpen, commonPorts[port])
                }
            }.awaitAll()
            emit(results.sortedBy { it.port })
        }
    }.flowOn(Dispatchers.IO)

    private fun isPortOpen(ip: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 500)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
