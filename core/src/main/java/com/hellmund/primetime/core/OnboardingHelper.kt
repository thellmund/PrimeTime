package com.hellmund.primetime.core

import android.content.SharedPreferences
import javax.inject.Inject

private const val KEY_FIRST_LAUNCH = "firstLaunchOfPrimeTime"

class OnboardingHelper @Inject constructor(
    private val sharedPrefs: SharedPreferences
) {

    var isFirstLaunch: Boolean
        get() = sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) {
            sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
        }

}
