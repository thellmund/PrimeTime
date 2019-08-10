package com.hellmund.primetime.onboarding

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import javax.inject.Inject

private const val KEY_FIRST_LAUNCH = "firstLaunchOfPrimeTime"

class OnboardingHelper @Inject constructor(
    private val context: Context
) {

    private val sharedPrefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    var isFirstLaunch: Boolean
        get() = sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) {
            sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
        }

}
