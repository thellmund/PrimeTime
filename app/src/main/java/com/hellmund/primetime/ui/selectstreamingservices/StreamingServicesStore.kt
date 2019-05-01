package com.hellmund.primetime.ui.selectstreamingservices

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import javax.inject.Inject

private const val KEY_STREAMING_SERVICES = "KEY_STREAMING_SERVICES"

interface StreamingServicesStore {
    fun getAll(): List<StreamingService>
    fun store(services: List<StreamingService>)
}

class RealStreamingServicesStore @Inject constructor(
        private val sharedPrefs: SharedPreferences
) : StreamingServicesStore {

    private val gson = Gson()

    override fun getAll(): List<StreamingService> {
        return sharedPrefs
                .getStringSet(KEY_STREAMING_SERVICES, emptySet())
                .map { gson.fromJson(it, StreamingService::class.java) }
                .toList()
                .sortedBy { it.name.toLowerCase() }
    }

    override fun store(services: List<StreamingService>) {
        sharedPrefs.edit {
            val values = services.map { gson.toJson(it) }.toSet()
            putStringSet(KEY_STREAMING_SERVICES, values)
        }
    }

}
