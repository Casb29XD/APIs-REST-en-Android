package com.desarrollomovielexample.actividadapirest.data.network
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class NetworkConnectivityObserver @Inject constructor(@ApplicationContext context: Context) {
    private val manager = context.getSystemService(ConnectivityManager::class.java)
    val isOnline: Flow<Boolean> = callbackFlow {
        fun emitStatus() {
            val caps = manager.getNetworkCapabilities(manager.activeNetwork)
            trySend(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = emitStatus()
            override fun onLost(network: Network) = emitStatus()
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = emitStatus()
        }
        emitStatus()
        manager.registerNetworkCallback(NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }
}