package com.masterflight.ping.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStreamReader

class TraceRepository {

    fun startTrace(host: String): Flow<TraceStep> = flow {
        // 在 Android 上，我们通常通过 ping 命令的 TTL 递增来模拟 traceroute
        // 因为很多设备没有直接的 traceroute 命令，或者权限受限
        for (ttl in 1..30) {
            val startTime = System.currentTimeMillis()
            val command = "ping -c 1 -t $ttl $host"
            var step: TraceStep? = null
            
            try {
                val process = Runtime.getRuntime().exec(command)
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                process.waitFor()
                val endTime = System.currentTimeMillis()
                val responseTime = (endTime - startTime).toDouble()

                val outputStr = output.toString()
                
                // 解析 IP 地址
                // 示例: From 192.168.1.1 icmp_seq=1 Time to live exceeded
                // 示例: 64 bytes from 8.8.8.8: icmp_seq=1 ttl=115 time=14.5 ms
                val ipMatch = "from ([^\\s:]+)".toRegex(RegexOption.IGNORE_CASE).find(outputStr)
                val ip = ipMatch?.groupValues?.get(1)

                if (outputStr.contains("Time to live exceeded") || outputStr.contains("time=")) {
                    step = TraceStep(
                        hop = ttl,
                        ip = ip,
                        responseTimeMs = responseTime,
                        isTimeout = false
                    )
                    emit(step)
                    
                    // 如果已经到达目的地（包含 time= 且 ip 就是目的地），则停止
                    if (outputStr.contains("time=") && !outputStr.contains("Time to live exceeded")) {
                        break
                    }
                } else {
                    emit(TraceStep(hop = ttl, isTimeout = true))
                }
            } catch (e: Exception) {
                emit(TraceStep(hop = ttl, isTimeout = true))
            }
        }
    }.flowOn(Dispatchers.IO)
}
