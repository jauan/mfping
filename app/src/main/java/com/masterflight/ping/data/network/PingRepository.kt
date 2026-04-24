package com.masterflight.ping.data.network

import android.content.Context
import com.masterflight.ping.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.BufferedReader
import java.io.InputStreamReader

class PingRepository(private val context: Context) {

    fun startPinging(host: String): Flow<PingResult> = flow {
        val command = "ping -i 1 $host"
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (currentCoroutineContext().isActive) {
                line = reader.readLine() ?: break
                if (line.contains("time=")) {
                    val result = parsePingLine(line)
                    if (result != null) {
                        emit(result)
                    }
                } else if (line.contains("timeout") || line.contains("Unreachable")) {
                    emit(PingResult.Error(context.getString(R.string.ping_timeout)))
                }
            }
        } catch (e: Exception) {
            emit(PingResult.Error(e.message ?: context.getString(R.string.unknown)))
        } finally {
            process?.destroy()
        }
    }.flowOn(Dispatchers.IO)

    private fun parsePingLine(line: String): PingResult? {
        return try {
            // Typical line: 64 bytes from 8.8.8.8: icmp_seq=1 ttl=115 time=14.5 ms
            val timeMatch = "time=([0-9.]+)".toRegex().find(line)
            val seqMatch = "icmp_seq=(\\d+)".toRegex().find(line)
            val ttlMatch = "ttl=(\\d+)".toRegex().find(line)

            if (timeMatch != null) {
                val time = timeMatch.groupValues[1].toDouble()
                val seq = seqMatch?.groupValues[1]?.toInt() ?: 0
                val ttl = ttlMatch?.groupValues[1]?.toInt() ?: 0
                PingResult.Success(time, seq, ttl)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
