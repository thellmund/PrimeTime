package com.hellmund.primetime.utils

import android.content.Context
import org.jetbrains.anko.defaultSharedPreferences

class OnboardingHelper(
        private val context: Context
) {

    var isFirstLaunch: Boolean
        get() = context.defaultSharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) {
            context.defaultSharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
        }

    companion object {
        private const val KEY_FIRST_LAUNCH = "firstLaunchOfPrimeTime"
    }

}
