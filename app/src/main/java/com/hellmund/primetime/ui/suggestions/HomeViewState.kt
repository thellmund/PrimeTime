package com.hellmund.primetime.ui.suggestions

import com.hellmund.primetime.data.model.Genre

data class Filter(
    val genres: List<Genre>,
    val checkedItems: BooleanArray
) {
    val genreNames: Array<String>
        get() = genres.map { it.name }.toTypedArray()
}

data class HomeViewState(
    val recommendationsType: RecommendationsType = RecommendationsType.Personalized(),
    val data: List<MovieViewEntity> = emptyList(),
    val filtered: List<MovieViewEntity>? = null,
    val pagesLoaded: Int = 0,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val showOnboardingBanner: Boolean = false,
    val showFilterButton: Boolean = false,
    val genresFilter: Filter? = null
) {

    fun toLoading(): HomeViewState {
        return copy(isLoading = true, error = null)
    }

    fun toError(t: Throwable): HomeViewState {
        return copy(isLoading = false, error = t)
    }

    fun toData(result: Result.Data): HomeViewState {
        val newData = data + result.data
        return copy(
            recommendationsType = result.type,
            data = newData,
            filtered = null,
            pagesLoaded = result.page,
            isLoading = false,
            error = null
        )
    }

    fun toData(result: Result.RatingStored): HomeViewState {
        val movies = data.minus(result.movie)
        return copy(data = movies)
    }

    fun toFiltered(result: Result.Filter): HomeViewState {
        val genreIds = result.genres.map { it.id }.toSet()
        val genreMovies = data.filter { genreIds.containsAny(it.raw.genreIds.orEmpty()) }
        val type = RecommendationsType.Personalized(result.genres)
        return copy(recommendationsType = type, filtered = genreMovies)
    }

    fun setShowOnboarding(showOnboarding: Boolean): HomeViewState {
        return copy(showOnboardingBanner = showOnboarding)
    }

    fun setShowFilterDialog(
        showFilter: Boolean,
        genres: List<Genre> = emptyList(),
        checkedItems: BooleanArray = emptyArray<Boolean>().toBooleanArray()
    ): HomeViewState {
        val filter = when(showFilter) {
            true -> Filter(genres, checkedItems)
            false -> null
        }
        return copy(genresFilter = filter)
    }

    fun setShowFilterButton(showFilterButton: Boolean): HomeViewState {
        return copy(showFilterButton = showFilterButton)
    }

    private fun <T> Set<T>.containsAny(
        elements: Collection<T>
    ): Boolean = elements.any { contains(it) }

}
