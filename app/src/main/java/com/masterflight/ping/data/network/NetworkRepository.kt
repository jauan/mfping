package com.masterflight.ping.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
    private val _networkStatus = MutableStateFlow(NetworkStatus(
        localIp = context.getString(R.string.unknown),
        publicIp = context.getString(R.string.loading),
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
            isConnected = connectionType != context.getString(R.string.conn_none)
        )

        // Fetch Public IP asynchronously
        val publicIp = fetchPublicIp()
        _networkStatus.value = _networkStatus.value.copy(publicIp = publicIp)
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

    private suspend fun fetchPublicIp(): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.ipify.org?format=json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext context.getString(R.string.error)
                val body = response.body?.string() ?: return@withContext context.getString(R.string.empty_body)
                val json = JSONObject(body)
                json.getString("ip")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context.getString(R.string.offline)
        }
    }
}
