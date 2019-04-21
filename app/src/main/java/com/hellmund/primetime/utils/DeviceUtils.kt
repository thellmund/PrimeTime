@file:JvmName("DeviceUtils")

package com.hellmund.primetime.utils

import android.content.Context

import android.content.res.Configuration.ORIENTATION_LANDSCAPE

val Context.isLandscapeMode: Boolean
    get() = resources.configuration.orientation == ORIENTATION_LANDSCAPE
