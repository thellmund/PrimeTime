package com.hellmund.primetime.utils

import android.content.Context
import org.jetbrains.anko.defaultSharedPreferences

class OnboardingHelper(
        private val context: Context
) {

    val isFirstLaunch: Boolean
        get() = context.defaultSharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)



    companion object {
        private const val KEY_FIRST_LAUNCH = "firstLaunchOfPrimeTime"
    }

}
