package com.hellmund.primetime.settings.delegates

import android.content.Context
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.core.Preferences
import com.hellmund.primetime.settings.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GenresDelegate @Inject constructor(
    private val context: Context,
    private val genresRepository: GenresRepository
) {

    suspend fun init(pref: MultiSelectListPreference) {
        val isIncludedGenres = pref.key == Preferences.KEY_INCLUDED

        val preferenceGenres = if (isIncludedGenres) {
            genresRepository.getPreferredGenres()
        } else {
            genresRepository.getExcludedGenres()
        }

        val values = preferenceGenres
            .map { it.id.toString() }
            .toSet()

        val genres = genresRepository.getAll()
        val genreIds = genres.map { it.id.toString() }.toTypedArray()
        val genreNames = genres.map { it.name }.toTypedArray()

        pref.entries = genreNames
        pref.entryValues = genreIds
        pref.values = values

        withContext(Dispatchers.Main) {
            updateGenresSummary(pref, preferenceGenres)
        }
    }

    fun updateGenresSummary(preference: Preference, genres: List<Genre>) {
        if (genres.isNotEmpty()) {
            preference.summary = genres.map { it.name }.sorted().joinToString(", ")
        } else {
            // Should only happen for excluded genres
            preference.summary = context.getString(R.string.excluded_genres_summary)
        }
    }

}
