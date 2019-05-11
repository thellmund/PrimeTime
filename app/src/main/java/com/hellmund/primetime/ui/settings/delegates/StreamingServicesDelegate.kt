package com.hellmund.primetime.ui.settings.delegates

import android.content.Context
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import com.hellmund.primetime.R
import com.hellmund.primetime.ui.selectstreamingservices.StreamingServicesStore
import javax.inject.Inject

class StreamingServicesDelegate @Inject constructor(
        private val context: Context,
        private val streamingServicesStore: StreamingServicesStore
) {

    fun init(pref: MultiSelectListPreference) {
        val streamingServices = streamingServicesStore.all
        val values = streamingServices
                .filter { it.isSelected }
                .map { it.name }
                .toSet()

        val entries = streamingServices.map { it.name }

        pref.entries = entries.toTypedArray()
        pref.entryValues = entries.toTypedArray()
        pref.values = values

        updateStreamingServicesSummary(pref, values)
    }

    fun updateStreamingServicesSummary(preference: Preference, values: Set<String>) {
        if (values.isNotEmpty()) {
            val summary = streamingServicesStore.all
                    .filter { it.name in values }
                    .map { it.name }
                    .sortedBy { it.toLowerCase() }
                    .joinToString(", ")
            preference.summary = summary
        } else {
            preference.summary = context.getString(R.string.streaming_services_summary)
        }
    }

}
