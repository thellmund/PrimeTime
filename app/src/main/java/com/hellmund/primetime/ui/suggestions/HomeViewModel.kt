package com.hellmund.primetime.ui.suggestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.shared.Reducer
import com.hellmund.primetime.ui.shared.ViewStateStore
import com.hellmund.primetime.ui.suggestions.RecommendationsType.Personalized
import com.hellmund.primetime.ui.suggestions.data.MovieRankingProcessor
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.utils.OnboardingHelper
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed class Action {
    data class LoadMovies(val page: Int = 1) : Action()
    object LoadMore : Action()
    data class StoreRating(val ratedMovie: RatedMovie) : Action()
    object ShowFilterDialog : Action()
    object HideFilterDialog : Action()
    data class Filter(val genres: List<Genre>) : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(
        val type: RecommendationsType,
        val data: List<MovieViewEntity>,
        val page: Int
    ) : Result()
    data class Error(val error: Throwable) : Result()
    data class RatingStored(val movie: MovieViewEntity) : Result()
    data class ShowFilterDialog(
        val genres: List<Genre>,
        val checkedItems: BooleanArray
    ) : Result()
    object HideFilterDialog : Result()
    data class Filter(val genres: List<Genre>) : Result()
    object ShowOnboardingBanner : Result()
    object HideOnboardingBanner : Result()
    object ShowFilterButton : Result()
}

class HomeViewStateReducer : Reducer<HomeViewState, Result> {
    override fun invoke(
        state: HomeViewState,
        result: Result
    ) = when (result) {
        is Result.Loading -> state.toLoading()
        is Result.Data -> state.toData(result)
        is Result.Error -> state.toError(result.error)
        is Result.RatingStored -> state.toData(result)
        is Result.ShowFilterDialog -> state.setShowFilterDialog(true, result.genres, result.checkedItems)
        is Result.HideFilterDialog -> state.setShowFilterDialog(false)
        is Result.Filter -> state.toFiltered(result)
        is Result.ShowOnboardingBanner -> state.setShowOnboarding(true)
        is Result.HideOnboardingBanner -> state.setShowOnboarding(false)
        is Result.ShowFilterButton -> state.setShowFilterButton(true)
    }
}

class HomeViewStateStore : ViewStateStore<HomeViewState, Result>(
    initialState = HomeViewState(),
    reducer = HomeViewStateReducer()
)

class HomeViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val genresRepository: GenresRepository,
    private val historyRepository: HistoryRepository,
    private val rankingProcessor: MovieRankingProcessor,
    private val viewEntitiesMapper: MovieViewEntitiesMapper,
    private val onboardingHelper: OnboardingHelper,
    private val recommendationsType: RecommendationsType
) : ViewModel() {

    private val store = HomeViewStateStore()
    val viewState: LiveData<HomeViewState> = store.viewState

    private var pagesLoaded: Int = 0
    private var isLoadingMore: Boolean = false

    init {
        viewModelScope.launch {
            store.dispatch(Result.Loading)
            showOnboardingIfNecessary()
            showFilterButtonIfNecessary()
            fetchRecommendations(pagesLoaded + 1)
        }
    }

    private fun showOnboardingIfNecessary() {
        if (onboardingHelper.isFirstLaunch && recommendationsType is Personalized) {
            store.dispatch(Result.ShowOnboardingBanner)
        }
    }

    private fun showFilterButtonIfNecessary() {
        if (onboardingHelper.isFirstLaunch && recommendationsType is Personalized) {
            store.dispatch(Result.ShowFilterButton)
        }
    }

    private suspend fun fetchRecommendations(
        page: Int
    ) {
        val result = try {
            val recommendations = repository.fetchRecommendations(recommendationsType, page)
            val ranked = rankingProcessor(recommendations, recommendationsType)
            val viewEntities = viewEntitiesMapper(ranked)
            Result.Data(recommendationsType, viewEntities, page)
        } catch (e: IOException) {
            Result.Error(e)
        }
        store.dispatch(result)
    }

    private suspend fun storeRating(ratedMovie: RatedMovie) {
        val historyMovie = HistoryMovie.from(ratedMovie)
        historyRepository.store(historyMovie)
        store.dispatch(Result.RatingStored(ratedMovie.movie))
    }

    private suspend fun showFilterDialog() {
        val genres = genresRepository.getPreferredGenres()
        val checkedItems = when (val type = recommendationsType) {
            is Personalized -> {
                val selectedGenres = type.genres ?: genres
                genres.map { selectedGenres.contains(it) }.toBooleanArray()
            }
            else -> genres.map { true }.toBooleanArray()
        }
        store.dispatch(Result.ShowFilterDialog(genres, checkedItems))
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.LoadMovies -> fetchRecommendations(action.page)
                is Action.LoadMore -> {
                    if (isLoadingMore.not()) {
                        isLoadingMore = true
                        fetchRecommendations(pagesLoaded + 1)
                        isLoadingMore = false
                    }
                }
                is Action.StoreRating -> storeRating(action.ratedMovie)
                is Action.ShowFilterDialog -> showFilterDialog()
                is Action.Filter -> store.dispatch(Result.Filter(action.genres))
                is Action.HideFilterDialog -> store.dispatch(Result.HideFilterDialog)
            }
        }
    }

}
