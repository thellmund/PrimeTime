package com.hellmund.primetime.core

import android.content.SharedPreferences
import javax.inject.Inject

private const val KEY_FIRST_LAUNCH = "firstLaunchOfPrimeTime"

class OnboardingHelper @Inject constructor(
    private val sharedPrefs: SharedPreferences
) {

    val isFirstLaunch: Boolean
        get() = sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)

    fun markFinished() {
        sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
}
