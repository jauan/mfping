package com.masterflight.ping.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.masterflight.ping.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface

class NetworkRepository(private val context: Context) {

    private val client = OkHttpClient()
    private val TAG = "NetworkRepo"

    private val _networkStatus = MutableStateFlow(NetworkStatus(
        localIp = context.getString(R.string.unknown),
        publicIp = context.getString(R.string.loading),
        location = "",
        connectionType = context.getString(R.string.unknown)
    ))
    val networkStatus: Flow<NetworkStatus> = _networkStatus.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    suspend fun refreshNetworkStatus() {
        val connectionType = getConnectionType()
        val localIp = getLocalIpAddress()
        
        _networkStatus.value = _networkStatus.value.copy(
            localIp = localIp,
            connectionType = connectionType,
            isConnected = connectionType != context.getString(R.string.conn_none),
            publicIp = context.getString(R.string.loading),
            location = context.getString(R.string.loading)
        )

        fetchPublicIpInfo()
    }

    private fun getConnectionType(): String {
        val network = connectivityManager.activeNetwork ?: return context.getString(R.string.conn_none)
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return context.getString(R.string.conn_none)
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> context.getString(R.string.conn_wifi)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> context.getString(R.string.conn_cellular)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> context.getString(R.string.conn_ethernet)
            else -> context.getString(R.string.conn_other)
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: context.getString(R.string.unknown)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return context.getString(R.string.unknown)
    }

    private suspend fun fetchPublicIpInfo() = withContext(Dispatchers.IO) {
        // 尝试多个 API 提高可靠性
        val apis = listOf(
            "https://ipwho.is/",
            "https://ipapi.co/json/",
            "https://freeipapi.com/api/json"
        )

        for (apiUrl in apis) {
            try {
                Log.d(TAG, "Attempting to fetch location from: $apiUrl")
                val request = Request.Builder().url(apiUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    
                    val (ip, location) = when {
                        apiUrl.contains("ipwho.is") -> {
                            val country = json.optString("country", "")
                            val region = json.optString("region", "")
                            val city = json.optString("city", "")
                            json.optString("ip") to listOf(country, region, city)
                        }
                        apiUrl.contains("ipapi.co") -> {
                            val country = json.optString("country_name", "")
                            val region = json.optString("region", "")
                            val city = json.optString("city", "")
                            json.optString("ip") to listOf(country, region, city)
                        }
                        else -> { // freeipapi
                            val country = json.optString("countryName", "")
                            val region = json.optString("regionName", "")
                            val city = json.optString("cityName", "")
                            json.optString("ipAddress") to listOf(country, region, city)
                        }
                    }

                    val locationStr = location.filter { it.isNotEmpty() && it != "null" }.joinToString(", ")
                    
                    _networkStatus.value = _networkStatus.value.copy(
                        publicIp = ip.ifEmpty { context.getString(R.string.unknown) },
                        location = locationStr
                    )
                    Log.i(TAG, "Successfully fetched location: $locationStr")
                    return@withContext // 成功后退出循环
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from $apiUrl: ${e.message}")
            }
        }

        // 如果所有 API 都失败
        _networkStatus.value = _networkStatus.value.copy(
            publicIp = context.getString(R.string.error),
            location = context.getString(R.string.offline)
        )
    }
}
