package com.hellmund.primetime.ui.suggestions

import com.hellmund.primetime.utils.containsAny

data class MainViewState(
    val recommendationsType: RecommendationsType = RecommendationsType.Personalized(),
    val data: List<MovieViewEntity> = emptyList(),
    val filtered: List<MovieViewEntity>? = null,
    val pagesLoaded: Int = 0,
    val isLoading: Boolean = false,
    val error: Throwable? = null
) {

    fun toLoading(): MainViewState {
        return copy(isLoading = true, error = null)
    }

    fun toError(t: Throwable): MainViewState {
        return copy(isLoading = false, error = t)
    }

    fun toData(result: Result.Data): MainViewState {
        val newData = if (result.page == 1) result.data else data + result.data
        return copy(
            recommendationsType = result.type,
            data = newData,
            filtered = null,
            pagesLoaded = result.page,
            isLoading = false,
            error = null
        )
    }

    fun toData(result: Result.RatingStored): MainViewState {
        val movies = data.minus(result.movie)
        return copy(data = movies)
    }

    fun toFiltered(result: Result.Filter): MainViewState {
        val genreIds = result.genres.map { it.id }.toSet()
        val genreMovies = data.filter { genreIds.containsAny(it.raw.genreIds.orEmpty()) }
        val type = RecommendationsType.Personalized(result.genres)
        return copy(recommendationsType = type, filtered = genreMovies)
    }

}
