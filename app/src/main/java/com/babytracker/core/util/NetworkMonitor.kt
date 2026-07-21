package com.babytracker.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(checkNetwork())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isUnmetered = MutableStateFlow(checkUnmetered())
    val isUnmetered: StateFlow<Boolean> = _isUnmetered.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun checkNetwork(): Boolean {
        return try {
            val network = cm?.activeNetwork ?: return true
            val caps = cm.getNetworkCapabilities(network) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    private fun checkUnmetered(): Boolean {
        return try {
            val network = cm?.activeNetwork ?: return true
            val caps = cm.getNetworkCapabilities(network) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } catch (_: Exception) {
            true
        }
    }

    private fun registerNetworkCallback() {
        try {
            cm?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                    _isUnmetered.value = checkUnmetered()
                }
                override fun onLost(network: Network) {
                    _isOnline.value = false
                    _isUnmetered.value = false
                }
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    _isOnline.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    _isUnmetered.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("NetworkMonitor", "registerDefaultNetworkCallback failed", e)
        }
    }
}
