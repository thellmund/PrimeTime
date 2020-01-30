package com.hellmund.primetime.recommendations.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.recommendations.data.MovieRankingProcessor
import com.hellmund.primetime.recommendations.data.MoviesRepository
import com.hellmund.primetime.ui_common.MovieViewEntitiesMapper
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.PartialMovieViewEntity
import com.hellmund.primetime.ui_common.RatedPartialMovie
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.SingleEvent
import com.hellmund.primetime.ui_common.viewmodel.SingleEventStore
import com.hellmund.primetime.ui_common.viewmodel.viewStateStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed class ViewEvent {
    data class LoadMovies(val page: Int = 1) : ViewEvent()
    data class LoadFullMovie(val movieId: Long) : ViewEvent()
    object LoadMore : ViewEvent()
    data class StoreRating(val ratedMovie: RatedPartialMovie) : ViewEvent()
    data class Filter(val genres: List<Genre>) : ViewEvent()
}

sealed class ViewResult {
    object Loading : ViewResult()
    data class Data(
        val type: RecommendationsType,
        val data: List<PartialMovieViewEntity>,
        val page: Int
    ) : ViewResult()
    data class Error(val error: Throwable) : ViewResult()
    data class RatingStored(val movie: PartialMovieViewEntity) : ViewResult()
    data class Filter(val genres: List<Genre>) : ViewResult()
    object ShowFilterButton : ViewResult()
    object HideFilterButton : ViewResult()
}

sealed class NavigationResult {
    data class ClickedMovieLoaded(val viewEntity: MovieViewEntity) : NavigationResult()
}

class HomeViewStateReducer : Reducer<HomeViewState, ViewResult> {
    override fun invoke(
        state: HomeViewState,
        viewResult: ViewResult
    ) = when (viewResult) {
        is ViewResult.Loading -> state.toLoading()
        is ViewResult.Data -> state.toData(viewResult)
        is ViewResult.Error -> state.toError(viewResult.error)
        is ViewResult.RatingStored -> state.toData(viewResult)
        is ViewResult.Filter -> state.toFiltered(viewResult)
        is ViewResult.ShowFilterButton -> state.copy(showFilterButton = true)
        is ViewResult.HideFilterButton -> state.copy(showFilterButton = false)
    }
}

class HomeViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val historyRepository: HistoryRepository,
    private val genresRepository: GenresRepository,
    private val rankingProcessor: MovieRankingProcessor,
    private val viewEntitiesMapper: MovieViewEntitiesMapper,
    private val recommendationsType: RecommendationsType
) : ViewModel() {

    private val store = viewStateStore(
        initialState = HomeViewState(),
        reducer = HomeViewStateReducer()
    )

    val viewState: LiveData<HomeViewState> = store.viewState

    private val navigationEventsStore = SingleEventStore<NavigationResult>()
    val navigationEvents: LiveData<SingleEvent<NavigationResult>> = navigationEventsStore.events

    private var isLoadingMore: Boolean = false

    private val pagesLoaded: Int
        get() = store.viewState.value?.pagesLoaded ?: 0

    init {
        viewModelScope.launch {
            store.dispatch(ViewResult.Loading)
            fetchRecommendations(recommendationsType, pagesLoaded + 1)

            genresRepository
                .observePreferredGenres()
                .map { if (it.isNotEmpty()) ViewResult.ShowFilterButton else ViewResult.HideFilterButton }
                .collect { store.dispatch(it) }
        }
    }

    private suspend fun fetchRecommendations(
        type: RecommendationsType,
        page: Int
    ) {
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

    private suspend fun storeRating(ratedMovie: RatedPartialMovie) {
        val historyMovie = ratedMovie.toHistoryMovie()
        historyRepository.store(historyMovie)
        store.dispatch(ViewResult.RatingStored(ratedMovie.movie))
    }

    private suspend fun loadFullMovie(movieId: Long) {
        val movie = checkNotNull(repository.fetchFullMovie(movieId))
        val viewEntity = viewEntitiesMapper(movie)
        navigationEventsStore.dispatch(NavigationResult.ClickedMovieLoaded(viewEntity))
    }

    fun dispatch(viewEvent: ViewEvent) {
        viewModelScope.launch {
            when (viewEvent) {
                is ViewEvent.LoadMovies -> fetchRecommendations(recommendationsType, viewEvent.page)
                is ViewEvent.LoadFullMovie -> loadFullMovie(viewEvent.movieId)
                is ViewEvent.LoadMore -> {
                    if (isLoadingMore.not()) {
                        isLoadingMore = true
                        fetchRecommendations(recommendationsType, pagesLoaded + 1)
                        isLoadingMore = false
                    }
                }
                is ViewEvent.Filter -> store.dispatch(ViewResult.Filter(viewEvent.genres))
                is ViewEvent.StoreRating -> storeRating(viewEvent.ratedMovie)
            }
        }
    }
}
