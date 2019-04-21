@file:JvmName("NetworkUtils")
package com.hellmund.primetime.utils

import android.content.Context
import org.jetbrains.anko.connectivityManager

val Context.isConnected: Boolean
    get() {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }
