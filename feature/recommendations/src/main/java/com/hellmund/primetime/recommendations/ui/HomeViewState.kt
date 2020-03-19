package com.hellmund.primetime.recommendations.ui

import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.ui_common.MovieViewEntity

data class HomeViewState(
    val recommendationsType: RecommendationsType = RecommendationsType.Personalized(),
    val data: List<MovieViewEntity.Partial> = emptyList(),
    val filtered: List<MovieViewEntity.Partial>? = null,
    val pagesLoaded: Int = 0,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val showFilterButton: Boolean = false,
    val showPersonalizationBanner: Boolean = false,
    val preferredGenres: List<Genre> = emptyList()
) {

    fun toLoading(): HomeViewState {
        return copy(isLoading = true, error = null)
    }

    fun toError(t: Throwable): HomeViewState {
        return copy(isLoading = false, error = t)
    }

    fun toData(viewResult: ViewResult.Data): HomeViewState {
        val newData = data + viewResult.data
        return copy(
            recommendationsType = viewResult.type,
            data = newData,
            filtered = null,
            pagesLoaded = viewResult.page,
            isLoading = false,
            error = null
        )
    }

    fun toData(viewResult: ViewResult.RatingStored): HomeViewState {
        val movies = data.minus(viewResult.movie)
        return copy(data = movies)
    }

    fun toFiltered(viewResult: ViewResult.Filter): HomeViewState {
        val genreIds = viewResult.genres.map { it.id }.toSet()
        val genreMovies = data.filter { genreIds.containsAny(it.raw.genreIds) }
        val type = RecommendationsType.Personalized(viewResult.genres)
        return copy(recommendationsType = type, filtered = genreMovies)
    }

    private fun <T> Set<T>.containsAny(
        elements: Collection<T>
    ): Boolean = elements.any { contains(it) }
}
