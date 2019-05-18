package com.hellmund.primetime.utils

import android.content.Context
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Inject

private const val KEY_FIRST_LAUNCH = "firstLaunchOfPrimeTime"

class OnboardingHelper @Inject constructor(
        private val context: Context
) {

    var isFirstLaunch: Boolean
        get() = context.defaultSharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) {
            context.defaultSharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
        }

}
