package com.hellmund.primetime.ui.suggestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.shared.Reducer
import com.hellmund.primetime.ui.shared.ViewStateStore
import com.hellmund.primetime.ui.suggestions.data.MovieRankingProcessor
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed class Action {
    data class LoadMovies(val page: Int = 1) : Action()
    object LoadMore : Action()
    data class StoreRating(val ratedMovie: RatedMovie) : Action()
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
    data class Filter(val genres: List<Genre>) : Result()
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
        is Result.Filter -> state.toFiltered(result)
    }
}

class HomeViewStateStore : ViewStateStore<HomeViewState, Result>(
    initialState = HomeViewState(),
    reducer = HomeViewStateReducer()
)

class HomeViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val historyRepository: HistoryRepository,
    private val rankingProcessor: MovieRankingProcessor,
    private val viewEntitiesMapper: MovieViewEntitiesMapper,
    private val recommendationsType: RecommendationsType
) : ViewModel() {

    private val store = HomeViewStateStore()
    val viewState: LiveData<HomeViewState> = store.viewState

    private var pagesLoaded: Int = 0
    private var isLoadingMore: Boolean = false

    init {
        viewModelScope.launch {
            store.dispatch(Result.Loading)
            fetchRecommendations(recommendationsType, pagesLoaded + 1)
        }
    }

    private suspend fun fetchRecommendations(
        type: RecommendationsType,
        page: Int
    ) {
        val result = try {
            val recommendations = repository.fetchRecommendations(type, page)
            val ranked = rankingProcessor(recommendations, type)
            val viewEntities = viewEntitiesMapper(ranked)
            Result.Data(type, viewEntities, page)
        } catch (e: IOException) {
            Result.Error(e)
        }
        store.dispatch(result)
    }

    private suspend fun storeRating(ratedMovie: RatedMovie) {
        val historyMovie = ratedMovie.toHistoryMovie()
        historyRepository.store(historyMovie)
        store.dispatch(Result.RatingStored(ratedMovie.movie))
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.LoadMovies -> fetchRecommendations(recommendationsType, action.page)
                is Action.LoadMore -> {
                    if (isLoadingMore.not()) {
                        isLoadingMore = true
                        fetchRecommendations(recommendationsType, pagesLoaded + 1)
                        isLoadingMore = false
                    }
                }
                is Action.Filter -> store.dispatch(Result.Filter(action.genres))
                is Action.StoreRating -> storeRating(action.ratedMovie)
            }
        }
    }

}
