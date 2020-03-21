package com.hellmund.primetime.settings.util

import androidx.core.content.edit
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.launch

fun <T : Preference> PreferenceFragmentCompat.requirePreference(key: String): T {
    return checkNotNull(findPreference(key))
}

fun Preference.doOnPreferenceChange(
    lifecycleScope: LifecycleCoroutineScope,
    block: suspend (Preference, Set<String>) -> Boolean
) {
    setOnPreferenceChangeListener { pref, newValue ->
        lifecycleScope.launch {
            @Suppress("UNCHECKED_CAST")
            val newValueSet = newValue as? Set<String> ?: return@launch
            val shouldUpdate = block(pref, newValueSet)

            if (shouldUpdate) {
                sharedPreferences.edit {
                    putStringSet(pref.key, newValue as Set<String>)
                }
            }
        }

        false
    }
}
