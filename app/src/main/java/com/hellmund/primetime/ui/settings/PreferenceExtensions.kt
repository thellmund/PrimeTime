package com.hellmund.primetime.ui.settings

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

fun <T : Preference> PreferenceFragmentCompat.requirePreference(key: String): T {
    return checkNotNull(findPreference(key))
}
