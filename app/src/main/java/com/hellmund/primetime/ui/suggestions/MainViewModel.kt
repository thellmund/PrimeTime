package com.hellmund.primetime.ui.suggestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.database.HistoryMovie
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

class MainViewStateReducer : Reducer<MainViewState, Result> {
    override fun invoke(
        state: MainViewState,
        result: Result
    ) = when (result) {
        is Result.Loading -> state.toLoading()
        is Result.Data -> state.toData(result) // TODO .also { pagesLoaded = it.pagesLoaded }
        is Result.Error -> state.toError(result.error)
        is Result.RatingStored -> state.toData(result)
        is Result.Filter -> state.toFiltered(result)
    }
}

class MainViewStateStore : ViewStateStore<MainViewState, Result>(
    initialState = MainViewState(),
    reducer = MainViewStateReducer()
)

class MainViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val historyRepository: HistoryRepository,
    private val rankingProcessor: MovieRankingProcessor,
    private val viewEntityMapper: MoviesViewEntityMapper,
    private val recommendationsType: RecommendationsType
) : ViewModel() {

    private val store = MainViewStateStore()
    val viewState: LiveData<MainViewState> = store.viewState

    private var pagesLoaded: Int = 0
    private var isLoadingMore: Boolean = false

    init {
        viewModelScope.launch {
            store.dispatch(Result.Loading)
            store.dispatch(fetchRecommendations(recommendationsType, pagesLoaded + 1))
        }
    }

    private suspend fun fetchRecommendations(
        type: RecommendationsType,
        page: Int
    ): Result {
        return try {
            val recommendations = repository.fetchRecommendations(type, page)
            val ranked = rankingProcessor(recommendations, type)
            val viewEntities = viewEntityMapper(ranked)
            Result.Data(type, viewEntities, page)
        } catch (e: IOException) {
            Result.Error(e)
        }
    }

    private suspend fun storeRating(ratedMovie: RatedMovie) {
        val historyMovie = HistoryMovie.from(ratedMovie)
        historyRepository.store(historyMovie)
        store.dispatch(Result.RatingStored(ratedMovie.movie))
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.LoadMovies -> {
                    if (isLoadingMore.not()) {
                        isLoadingMore = true
                        fetchRecommendations(recommendationsType, action.page)
                    }
                }
                is Action.LoadMore -> {
                    if (isLoadingMore.not()) {
                        isLoadingMore = true
                        fetchRecommendations(recommendationsType, pagesLoaded + 1)
                    }
                }
                is Action.Filter -> store.dispatch(Result.Filter(action.genres))
                is Action.StoreRating -> storeRating(action.ratedMovie)
            }
        }
    }

}
