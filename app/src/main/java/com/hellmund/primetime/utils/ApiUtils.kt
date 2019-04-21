package com.hellmund.primetime.utils

import com.hellmund.primetime.BuildConfig
import java.util.*

object ApiUtils {

    val apiKey: String
        get() = BuildConfig.API_KEY

    val language: String
        get() = Locale.getDefault().language

}
