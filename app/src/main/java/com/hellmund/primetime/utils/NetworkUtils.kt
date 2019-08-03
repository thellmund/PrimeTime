@file:JvmName("NetworkUtils")

package com.hellmund.primetime.utils

import android.content.Context
import android.net.NetworkCapabilities
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import org.jetbrains.anko.connectivityManager

val Context.isConnected: Boolean
    get() {
        return if (SDK_INT >= M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }
