package com.infranite.app.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.util.Log

class ConnectivityObserver(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val handler = Handler(Looper.getMainLooper())
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("ConnectivityObserver", "Network available")
            notifyNetworkStatus(true)
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d("ConnectivityObserver", "Network lost")
            notifyNetworkStatus(false)
        }
    }
    
    fun observeConnectivity() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    fun stopObserving() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    
    private fun notifyNetworkStatus(isOnline: Boolean) {
        Log.d("ConnectivityObserver", "Network status: ${if (isOnline) "Online" else "Offline"}")
    }
}
