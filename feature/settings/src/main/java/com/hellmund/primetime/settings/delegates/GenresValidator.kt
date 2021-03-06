package com.hellmund.primetime.settings.delegates

import androidx.preference.Preference
import com.hellmund.primetime.core.Preferences
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.repositories.GenresRepository
import java.util.Collections
import javax.inject.Inject

private const val MIN_GENRES = 2

sealed class ValidationResult {
    data class Success(val genres: List<Genre>) : ValidationResult()
    data class Overlap(val genres: List<Genre>) : ValidationResult()
    object NotEnough : ValidationResult()
}

class GenresValidator @Inject constructor(
    private val genresRepository: GenresRepository
) {

    suspend fun validate(pref: Preference, genreIds: Set<String>): ValidationResult {
        val isPreferredGenres = pref.key == Preferences.KEY_INCLUDED
        val isExcludedGenres = pref.key == Preferences.KEY_EXCLUDED

        val enoughChecked = if (isPreferredGenres) enoughGenresChecked(genreIds) else true

        return if (enoughChecked && genresAreDisjoint(pref, genreIds)) {
            val genres = getGenresFromValues(genreIds).map { genre ->
                Genre.Impl(
                    id = genre.id,
                    name = genre.name,
                    isPreferred = isPreferredGenres,
                    isExcluded = isExcludedGenres
                )
            }
            val results = genres.filter { if (isPreferredGenres) it.isPreferred else it.isExcluded }
            ValidationResult.Success(results)
        } else if (!enoughGenresChecked(genreIds)) {
            ValidationResult.NotEnough
        } else {
            val overlap = getOverlappingGenres(pref, genreIds)
            ValidationResult.Overlap(overlap)
        }
    }

    private suspend fun getOverlappingGenres(
        pref: Preference,
        newValues: Set<String>
    ): List<Genre> {
        val isIncludedGenres = pref.key == Preferences.KEY_INCLUDED

        val includedGenres = if (isIncludedGenres) {
            genresRepository.getPreferredGenres().toMutableSet()
        } else {
            genresRepository.getGenres(newValues).toMutableSet()
        }

        val excludedGenres = if (isIncludedGenres.not()) {
            genresRepository.getExcludedGenres().toMutableSet()
        } else {
            genresRepository.getGenres(newValues).toMutableSet()
        }

        includedGenres.retainAll(excludedGenres)
        return includedGenres.sortedBy { it.name }
    }

    private suspend fun getGenresFromValues(values: Set<String>): List<Genre> {
        return values
            .map { genresRepository.getGenreById(it.toLong()) }
            .sortedBy { it.name }
    }

    private fun enoughGenresChecked(newGenres: Set<String>): Boolean {
        return newGenres.size >= MIN_GENRES
    }

    private suspend fun genresAreDisjoint(
        preference: Preference,
        newGenres: Set<String>
    ): Boolean {
        val includedGenres = genresRepository.getPreferredGenres()
        val excludedGenres = genresRepository.getExcludedGenres()

        val includedIds = includedGenres.map { it.id.toString() }.toMutableSet()
        val excludedIds = excludedGenres.map { it.id.toString() }.toMutableSet()

        if (preference.key == Preferences.KEY_INCLUDED) {
            includedIds.clear()
            includedIds += newGenres
        } else {
            excludedIds.clear()
            excludedIds += newGenres
        }

        return excludedIds.isEmpty() || Collections.disjoint(includedIds, excludedIds)
    }
}
