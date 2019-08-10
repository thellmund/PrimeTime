@file:JvmName("NetworkUtils")

package com.hellmund.primetime.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import androidx.core.content.getSystemService

val Context.isConnected: Boolean
    get() {
        val connectivityManager = checkNotNull(getSystemService<ConnectivityManager>())
        return if (SDK_INT >= M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }
