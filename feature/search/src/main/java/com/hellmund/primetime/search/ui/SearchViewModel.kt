package com.hellmund.primetime.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.core.Intents
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.search.R
import com.hellmund.primetime.search.data.SearchRepository
import com.hellmund.primetime.ui_common.MovieViewEntitiesMapper
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.PartialMovieViewEntity
import com.hellmund.primetime.ui_common.RatedPartialMovie
import com.hellmund.primetime.ui_common.viewmodel.SingleEvent
import com.hellmund.primetime.ui_common.viewmodel.SingleEventStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed class ViewEvent {
    data class Search(val query: String) : ViewEvent()
    data class TextChanged(val text: String) : ViewEvent()
    data class AddToHistory(val ratedMovie: RatedPartialMovie) : ViewEvent()
    data class CategorySelected(val category: String) : ViewEvent()
    data class ProcessExtra(val extra: String) : ViewEvent()
    data class MovieClicked(val viewEntity: PartialMovieViewEntity) : ViewEvent()
}

sealed class ViewResult {
    object Loading : ViewResult()
    data class GenresLoaded(val genres: List<Genre>) : ViewResult()
    data class Data(val data: List<PartialMovieViewEntity>) : ViewResult()
    data class Error(val error: Throwable) : ViewResult()
    data class ToggleClearButton(val show: Boolean) : ViewResult()
    data class ShowSnackbar(val messageResId: Int) : ViewResult()
    object DismissSnackbar : ViewResult()
}

sealed class NavigationResult {
    data class OpenMovieDetails(val viewEntity: MovieViewEntity) : NavigationResult()
    data class OpenCategory(val recommendationsType: RecommendationsType) : NavigationResult()
}

class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val historyRepository: HistoryRepository,
    private val genresRepository: GenresRepository,
    private val viewEntitiesMapper: MovieViewEntitiesMapper
) : ViewModel() {

    private val store = SearchViewStateStore()
    val viewState: LiveData<SearchViewState> = store.viewState

    private val navigationResultsStore = SingleEventStore<NavigationResult>()
    val navigationResults: LiveData<SingleEvent<NavigationResult>> = navigationResultsStore.events

    init {
        viewModelScope.launch {
            store.dispatch(fetchGenres())
        }
    }

    private suspend fun processNavigation(category: String) {
        val recommendationsType = when (category) {
            "Now playing" -> RecommendationsType.NowPlaying
            "Upcoming" -> RecommendationsType.Upcoming
            else -> {
                val genre = genresRepository.getGenreByName(category)
                RecommendationsType.ByGenre(genre)
            }
        }

        navigationResultsStore.dispatch(NavigationResult.OpenCategory(recommendationsType))
    }

    private suspend fun fetchGenres(): ViewResult {
        val genres = genresRepository.getAll()
        return ViewResult.GenresLoaded(genres)
    }

    private suspend fun searchMovies(query: String): ViewResult {
        return try {
            val movies = repository.searchMovies(query)
            val mapped = viewEntitiesMapper.mapPartialMovies(movies)
            ViewResult.Data(mapped)
        } catch (e: IOException) {
            ViewResult.Error(e)
        }
    }

    private suspend fun storeInHistory(historyMovie: HistoryMovie): ViewResult {
        val messageResId = when (historyMovie.rating) {
            Rating.Like -> R.string.will_more_like_this
            Rating.Dislike -> R.string.will_less_like_this
        }

        historyRepository.store(historyMovie)
        return ViewResult.ShowSnackbar(messageResId)
    }

    private suspend fun search(query: String) {
        store.dispatch(searchMovies(query))
    }

    private fun onTextChanged(input: String) {
        store.dispatch(ViewResult.ToggleClearButton(input.isNotEmpty()))
    }

    private suspend fun addToHistory(historyMovie: HistoryMovie) {
        store.dispatch(storeInHistory(historyMovie))
        delay(4_000)
        store.dispatch(ViewResult.DismissSnackbar)
    }

    private suspend fun onCategorySelected(category: String) {
        processNavigation(category)
    }

    private suspend fun processExtra(extra: String) {
        val recommendationsType = getRecommendationsTypeFromExtra(extra)
        navigationResultsStore.dispatch(NavigationResult.OpenCategory(recommendationsType))
    }

    private suspend fun loadFullMovie(movieId: Long) {
        val movie = checkNotNull(repository.fetchFullMovie(movieId))
        val viewEntity = viewEntitiesMapper(movie)
        navigationResultsStore.dispatch(NavigationResult.OpenMovieDetails(viewEntity))
    }

    fun dispatch(viewEvent: ViewEvent) {
        viewModelScope.launch {
            when (viewEvent) {
                is ViewEvent.AddToHistory -> addToHistory(viewEvent.ratedMovie.toHistoryMovie())
                is ViewEvent.CategorySelected -> onCategorySelected(viewEvent.category)
                is ViewEvent.ProcessExtra -> processExtra(viewEvent.extra)
                is ViewEvent.Search -> search(viewEvent.query)
                is ViewEvent.TextChanged -> onTextChanged(viewEvent.text)
                is ViewEvent.MovieClicked -> loadFullMovie(viewEvent.viewEntity.id)
            }
        }
    }

    private suspend fun getRecommendationsTypeFromExtra(
        extra: String
    ): RecommendationsType = when (extra) {
        Intents.NOW_PLAYING -> RecommendationsType.NowPlaying
        Intents.UPCOMING -> RecommendationsType.Upcoming
        else -> {
            val genre = genresRepository.getGenreByName(extra)
            RecommendationsType.ByGenre(genre)
        }
    }
}
