package com.hellmund.primetime.settings.util

import androidx.core.content.edit
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.launch

fun <T : Preference> PreferenceFragmentCompat.requirePreference(key: String): T {
    return checkNotNull(findPreference(key))
}

@Suppress("UNCHECKED_CAST")
fun Preference.doOnPreferenceChange(
    lifecycleScope: LifecycleCoroutineScope,
    block: suspend (Preference, Any) -> Boolean
) {
    setOnPreferenceChangeListener { pref, newValue ->
        lifecycleScope.launch {
            val shouldUpdate = block(pref, newValue)
            if (shouldUpdate) {
                sharedPreferences.edit {
                    putStringSet(pref.key, newValue as Set<String>)
                }
            }
        }

        false
    }
}
