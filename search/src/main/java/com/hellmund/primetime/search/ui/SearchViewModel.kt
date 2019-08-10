package com.hellmund.primetime.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.GenresRepository
import com.hellmund.primetime.data.HistoryRepository
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.search.R
import com.hellmund.primetime.search.data.SearchRepository
import com.hellmund.primetime.search.util.StringProvider
import com.hellmund.primetime.ui_common.SingleLiveDataEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed class Result {
    object Loading : Result()
    data class GenresLoaded(val genres: List<Genre>) : Result()
    data class Data(val data: List<SearchViewEntity>) : Result()
    data class Error(val error: Throwable) : Result()
    data class ToggleClearButton(val show: Boolean) : Result()
    data class ShowSnackbar(val message: String) : Result()
    object DismissSnackbar : Result()
}

class NavigationEvent(
    value: RecommendationsType
) : SingleLiveDataEvent<RecommendationsType>(value)

class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val historyRepository: HistoryRepository,
    private val genresRepository: GenresRepository,
    private val viewEntitiesMapper: SearchViewEntitiesMapper,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val store = SearchViewStateStore()
    val viewState: LiveData<SearchViewState> = store.viewState

    private val _events = MutableLiveData<NavigationEvent>()
    val destinations: LiveData<NavigationEvent> = _events

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

        _events.value = NavigationEvent(recommendationsType)
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
