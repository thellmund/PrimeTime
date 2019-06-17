package com.hellmund.primetime.ui.settings.delegates

import android.content.Context
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.utils.Constants
import javax.inject.Inject

class GenresDelegate @Inject constructor(
        private val context: Context,
        private val genresRepository: GenresRepository
) {

    fun init(pref: MultiSelectListPreference) {
        val isIncludedGenres = pref.key == Constants.KEY_INCLUDED

        val preferenceGenres = if (isIncludedGenres) {
            emptyList<Genre>() // TODO genresRepository.preferredGenres.blockingFirst()
        } else {
            emptyList<Genre>() // TODO genresRepository.excludedGenres.blockingFirst()
        }

        val values = preferenceGenres
                .map { it.id.toString() }
                .toSet()

        val genres = genresRepository.all.blockingGet()
        val genreIds = genres.map { it.id.toString() }.toTypedArray()
        val genreNames = genres.map { it.name }.toTypedArray()

        pref.entries = genreNames
        pref.entryValues = genreIds
        pref.values = values

        updateGenresSummary(pref, preferenceGenres)
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
