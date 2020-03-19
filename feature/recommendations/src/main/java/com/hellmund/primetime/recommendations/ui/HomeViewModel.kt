package com.hellmund.primetime.recommendations.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.core.OnboardingHelper
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.recommendations.data.MovieRankingProcessor
import com.hellmund.primetime.recommendations.data.MoviesRepository
import com.hellmund.primetime.ui_common.MovieViewEntitiesMapper
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.RatedMovie
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.viewStateStore
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed class ViewEvent {
    data class LoadMovies(val page: Int = 1) : ViewEvent()
    object LoadMore : ViewEvent()
    data class StoreRating(val ratedMovie: RatedMovie.Partial) : ViewEvent()
    data class Filter(val genres: List<Genre>) : ViewEvent()
    object DismissPersonalizationBanner : ViewEvent()
}

sealed class ViewResult {
    object Loading : ViewResult()
    object LoadingMore : ViewResult()
    data class Data(
        val type: RecommendationsType,
        val data: List<MovieViewEntity.Partial>,
        val page: Int
    ) : ViewResult()
    data class Error(val error: Throwable) : ViewResult()
    data class RatingStored(val movie: MovieViewEntity.Partial) : ViewResult()
    data class Filter(val genres: List<Genre>) : ViewResult()
    object ShowFilterButton : ViewResult()
    object HideFilterButton : ViewResult()
    object ShowPersonalizationBanner : ViewResult()
    object HidePersonalizationBanner : ViewResult()
    data class PreferredGenresLoaded(val genres: List<Genre>) : ViewResult()
}

class HomeViewStateReducer : Reducer<HomeViewState, ViewResult> {
    override fun invoke(
        state: HomeViewState,
        viewResult: ViewResult
    ) = when (viewResult) {
        is ViewResult.Loading -> state.copy(isLoading = true)
        is ViewResult.LoadingMore -> state.copy(isLoadingMore = true)
        is ViewResult.Data -> state.copy(
            recommendationsType = viewResult.type,
            data = state.data + viewResult.data,
            filtered = null,
            pagesLoaded = viewResult.page,
            isLoading = false,
            isLoadingMore = false,
            error = null
        )
        is ViewResult.Error -> state.copy(
            isLoading = false,
            isLoadingMore = false,
            error = viewResult.error
        )
        is ViewResult.RatingStored -> state.copy(data = state.data.minus(viewResult.movie))
        is ViewResult.Filter -> state.copy(
            recommendationsType = RecommendationsType.Personalized(viewResult.genres),
            filtered = state.data.filterWithGenres(viewResult.genres)
        )
        is ViewResult.ShowFilterButton -> state.copy(showFilterButton = true)
        is ViewResult.HideFilterButton -> state.copy(showFilterButton = false)
        is ViewResult.ShowPersonalizationBanner -> state.copy(showPersonalizationBanner = true)
        is ViewResult.HidePersonalizationBanner -> state.copy(showPersonalizationBanner = false)
        is ViewResult.PreferredGenresLoaded -> state.copy(preferredGenres = viewResult.genres)
    }
}

private fun List<MovieViewEntity.Partial>.filterWithGenres(genres: List<Genre>): List<MovieViewEntity.Partial> {
    val genreIds = genres.map { it.id }.toSet()
    return filter { genreIds.containsAny(it.raw.genreIds) }
}

private fun <T> Set<T>.containsAny(
    elements: Collection<T>
): Boolean = elements.any { contains(it) }

class HomeViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val historyRepository: HistoryRepository,
    private val genresRepository: GenresRepository,
    private val rankingProcessor: MovieRankingProcessor,
    private val viewEntitiesMapper: MovieViewEntitiesMapper,
    private val recommendationsType: RecommendationsType,
    private val onboardingHelper: OnboardingHelper
) : ViewModel() {

    private val store = viewStateStore(
        initialState = HomeViewState(),
        reducer = HomeViewStateReducer()
    )

    val viewState: LiveData<HomeViewState> = store.viewState

    init {
        viewModelScope.launch {
            store.dispatch(ViewResult.Loading)
            fetchRecommendations(recommendationsType)
        }

        viewModelScope.launch {
            genresRepository.observePreferredGenres().collect {
                store += ViewResult.PreferredGenresLoaded(it)
                store += determinePersonalizationBannerState(it)
                store += determineFilterButtonVisibility(it)
            }
        }
    }

    private fun determineFilterButtonVisibility(preferredGenres: List<Genre>): ViewResult {
        val hasSelectedPreferredGenres = preferredGenres.isNotEmpty()
        val isPersonalized = recommendationsType is RecommendationsType.Personalized
        val showFilterButton = isPersonalized && hasSelectedPreferredGenres
        return if (showFilterButton) ViewResult.ShowFilterButton else ViewResult.HideFilterButton
    }

    private fun determinePersonalizationBannerState(preferredGenres: List<Genre>): ViewResult {
        val hasNotSelectedPreferredGenres = preferredGenres.isEmpty()
        val isPersonalized = recommendationsType is RecommendationsType.Personalized
        val showFilterButton = isPersonalized && hasNotSelectedPreferredGenres

        return if (showFilterButton) {
            ViewResult.ShowPersonalizationBanner
        } else {
            ViewResult.HidePersonalizationBanner
        }
    }

    private fun fetchRecommendations(
        type: RecommendationsType,
        page: Int = 1
    ) = viewModelScope.launch {
        val result = try {
            val recommendations = repository.fetchRecommendations(type, page)
            val ranked = rankingProcessor(recommendations, type)
            val viewEntities = viewEntitiesMapper.mapPartialMovies(ranked)
            ViewResult.Data(type, viewEntities, page)
        } catch (e: IOException) {
            ViewResult.Error(e)
        }
        store.dispatch(result)
    }

    private fun loadMoreRecommendations(
        type: RecommendationsType,
        isLoadingMore: Boolean,
        page: Int
    ) = viewModelScope.launch {
        if (isLoadingMore.not()) {
            store += ViewResult.LoadingMore
            fetchRecommendations(type, page)
        }
    }

    private fun storeRating(ratedMovie: RatedMovie.Partial) = viewModelScope.launch {
        val historyMovie = ratedMovie.toHistoryMovie()
        historyRepository.store(historyMovie)
        store.dispatch(ViewResult.RatingStored(ratedMovie.movie))
    }

    fun handleViewEvent(viewEvent: ViewEvent) {
        when (viewEvent) {
            is ViewEvent.LoadMovies -> fetchRecommendations(recommendationsType, viewEvent.page)
            is ViewEvent.LoadMore -> {
                val viewState = store.currentViewState
                val isLoadingMore = viewState.isLoadingMore
                val pageToLoad = viewState.pagesLoaded + 1
                loadMoreRecommendations(recommendationsType, isLoadingMore, pageToLoad)
            }
            is ViewEvent.Filter -> store.dispatch(ViewResult.Filter(viewEvent.genres))
            is ViewEvent.StoreRating -> storeRating(viewEvent.ratedMovie)
            is ViewEvent.DismissPersonalizationBanner -> onboardingHelper.markFinished()
        }
    }
}
