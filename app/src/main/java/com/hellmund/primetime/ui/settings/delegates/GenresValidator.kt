package com.hellmund.primetime.ui.settings.delegates

import androidx.preference.Preference
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.utils.Constants
import io.reactivex.disposables.CompositeDisposable
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

    private val compositeDisposable = CompositeDisposable()

    fun validate(pref: Preference, newValue: Any): ValidationResult {
        val genreIds = newValue as Set<String>

        val isIncludedGenres = pref.key == Constants.KEY_INCLUDED
        val isExcludedGenres = pref.key == Constants.KEY_EXCLUDED

        val enoughChecked = if (isIncludedGenres) enoughGenresChecked(genreIds) else true

        return if (enoughChecked && genresAreDisjoint(pref, newValue)) {
            val genres = getGenresFromValues(genreIds)

            for (genre in genres) {
                genre.isPreferred = isIncludedGenres
                genre.isExcluded = isExcludedGenres
            }

            // compositeDisposable += genresRepository.storeGenres(genres).subscribe()

            val results = genres.filter { if (isIncludedGenres) it.isPreferred else it.isExcluded }
            ValidationResult.Success(results)
        } else if (!enoughGenresChecked(newValue)) {
            ValidationResult.NotEnough
        } else {
            val overlap = getOverlappingGenres(pref, genreIds)
            ValidationResult.Overlap(overlap)
        }
    }

    private fun getOverlappingGenres(pref: Preference, newValues: Set<String>): List<Genre> {
        val isIncludedGenres = pref.key == Constants.KEY_INCLUDED

        val includedGenres = if (isIncludedGenres) {
            mutableSetOf<Genre>() // TODO genresRepository.preferredGenres.blockingFirst().toMutableSet()
        } else {
            genresRepository.getGenres(newValues).blockingGet().toMutableSet()
        }

        val excludedGenres = if (isIncludedGenres.not()) {
            mutableSetOf<Genre>() // TODO genresRepository.excludedGenres.blockingFirst().toMutableSet()
        } else {
            genresRepository.getGenres(newValues).blockingGet().toMutableSet()
        }

        includedGenres.retainAll(excludedGenres)
        return includedGenres.sortedBy { it.name }
    }

    private fun getGenresFromValues(values: Set<String>): List<Genre> {
        return values
            .map { genresRepository.getGenre(it).blockingGet() }
            .sortedBy { it.name }
    }

    private fun enoughGenresChecked(newGenres: Set<String>): Boolean {
        return newGenres.size >= MIN_GENRES
    }

    private fun genresAreDisjoint(preference: Preference, newGenres: Set<String>): Boolean {
        val includedGenres = listOf<Genre>() // genresRepository.preferredGenres.blockingFirst()
        val excludedGenres = listOf<Genre>() // genresRepository.excludedGenres.blockingFirst()

        val includedIds = includedGenres.map { it.id.toString() }.toMutableSet()
        val excludedIds = excludedGenres.map { it.id.toString() }.toMutableSet()

        if (preference.key == Constants.KEY_INCLUDED) {
            includedIds.clear()
            includedIds += newGenres
        } else {
            excludedIds.clear()
            excludedIds += newGenres
        }

        return excludedIds.isEmpty() || Collections.disjoint(includedIds, excludedIds)
    }

    fun cancel() {
        compositeDisposable.dispose()
    }

}
