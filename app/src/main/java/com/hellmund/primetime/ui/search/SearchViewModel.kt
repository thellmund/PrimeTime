package com.hellmund.primetime.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.ApiGenre
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.shared.NavigationEvent
import com.hellmund.primetime.ui.shared.NavigationEventsStore
import com.hellmund.primetime.ui.shared.Reducer
import com.hellmund.primetime.ui.shared.ViewStateStore
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.MovieViewEntitiesMapper
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.utils.StringProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class SearchViewState(
    val genres: List<Genre> = emptyList(),
    val data: List<MovieViewEntity> = emptyList(),
    val showClearButton: Boolean = false,
    val didPerformSearch: Boolean = false,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val snackbarText: String? = null
) {

    val showPlaceholder: Boolean
        get() = data.isEmpty() && didPerformSearch

}

sealed class Result {
    object Loading : Result()
    data class GenresLoaded(val genres: List<Genre>) : Result()
    data class Data(val data: List<MovieViewEntity>) : Result()
    data class Error(val error: Throwable) : Result()
    data class ToggleClearButton(val show: Boolean) : Result()
    data class ShowSnackbar(val message: String) : Result()
    object DismissSnackbar : Result()
}

class SearchViewStateReducer : Reducer<SearchViewState, Result> {
    override fun invoke(
        state: SearchViewState,
        result: Result
    ) = when (result) {
        is Result.GenresLoaded -> state.copy(genres = result.genres)
        is Result.Loading -> state.copy(isLoading = true, error = null)
        is Result.Data -> state.copy(data = result.data, isLoading = false, error = null, didPerformSearch = true)
        is Result.Error -> state.copy(isLoading = false, error = result.error, didPerformSearch = true)
        is Result.ToggleClearButton -> state.copy(showClearButton = result.show)
        is Result.ShowSnackbar -> state.copy(snackbarText = result.message)
        is Result.DismissSnackbar -> state.copy(snackbarText = null)
    }
}

class SearchViewStateStore : ViewStateStore<SearchViewState, Result>(
    initialState = SearchViewState(),
    reducer = SearchViewStateReducer()
)

class SearchViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val historyRepository: HistoryRepository,
    private val genresRepository: GenresRepository,
    private val viewEntitiesMapper: MovieViewEntitiesMapper,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val store = SearchViewStateStore()
    val viewState: LiveData<SearchViewState> = store.viewState

    private val events = NavigationEventsStore()
    val destinations: LiveData<NavigationEvent> = events.viewEvents

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
                val apiGenre = ApiGenre(genre.id, genre.name)
                RecommendationsType.ByGenre(apiGenre)
            }
        }

        events.dispatch(NavigationEvent(recommendationsType))
    }

    private suspend fun fetchGenres(): Result {
        val genres = genresRepository.getAll()
        return Result.GenresLoaded(genres)
    }

    private suspend fun searchMovies(query: String): Result {
        return try {
            val movies = repository.searchMovies(query)
            val mapped = viewEntitiesMapper(movies)
            Result.Data(mapped)
        } catch (e: IOException) {
            Result.Error(e)
        }
    }

    private suspend fun storeInHistory(historyMovie: HistoryMovie): Result {
        val messageResId = when (historyMovie.rating) {
            Rating.Like -> R.string.will_more_like_this
            Rating.Dislike -> R.string.will_less_like_this
        }
        historyRepository.store(historyMovie)

        val message = stringProvider.getString(messageResId)
        return Result.ShowSnackbar(message)
    }

    fun search(query: String) {
        viewModelScope.launch {
            store.dispatch(searchMovies(query))
        }
    }

    fun onTextChanged(input: String) {
        viewModelScope.launch {
            store.dispatch(Result.ToggleClearButton(input.isNotEmpty()))
        }
    }

    fun addToHistory(historyMovie: HistoryMovie) {
        viewModelScope.launch {
            store.dispatch(storeInHistory(historyMovie))
            delay(4_000)
            store.dispatch(Result.DismissSnackbar)
        }
    }

    fun onCategorySelected(category: String) {
        viewModelScope.launch {
            processNavigation(category)
        }
    }

}
