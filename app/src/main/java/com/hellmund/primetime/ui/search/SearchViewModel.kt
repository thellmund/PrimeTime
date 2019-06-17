package com.hellmund.primetime.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.ApiGenre
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.shared.ViewStateStore
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.MoviesViewEntityMapper
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.utils.StringProvider
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
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

class SearchViewStateStore : ViewStateStore<SearchViewState, Result>(SearchViewState()) {

    override fun reduceState(
        state: SearchViewState,
        result: Result
    ): SearchViewState {
        return when (result) {
            is Result.GenresLoaded -> state.copy(genres = result.genres)
            is Result.Loading -> state.copy(isLoading = true, error = null)
            is Result.Data -> state.copy(data = result.data, isLoading = false, error = null, didPerformSearch = true)
            is Result.Error -> state.copy(isLoading = false, error = result.error, didPerformSearch = true)
            is Result.ToggleClearButton -> state.copy(showClearButton = result.show)
            is Result.ShowSnackbar -> state.copy(snackbarText = result.message)
            is Result.DismissSnackbar -> state.copy(snackbarText = null)
        }
    }

}

class SearchViewModel @Inject constructor(
        private val repository: MoviesRepository,
        private val historyRepository: HistoryRepository,
        private val genresRepository: GenresRepository,
        private val viewEntityMapper: MoviesViewEntityMapper,
        private val stringProvider: StringProvider
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val navigationRelay = PublishRelay.create<String>()

    private val store = SearchViewStateStore()
    val viewState: LiveData<SearchViewState> = store.viewState

    private val _destinations = MutableLiveData<NavigationEvent>()
    val destinations: LiveData<NavigationEvent> = _destinations

    init {
        viewModelScope.launch {
            store.dispatch(fetchGenres())
        }

        compositeDisposable += navigationRelay
                .switchMap(this::processNavigation)
                .subscribe(this::navigate)
    }

    private fun processNavigation(category: String): Observable<RecommendationsType> {
        return when (category) {
            "Now playing" -> Observable.just(RecommendationsType.NowPlaying)
            "Upcoming" -> Observable.just(RecommendationsType.Upcoming)
            else -> {
                genresRepository
                        .getGenreByName(category)
                        .map { ApiGenre(it.id, it.name) }
                        .map { RecommendationsType.ByGenre(it) as RecommendationsType }
                        .toObservable()
            }
        }
    }

    private suspend fun fetchGenres(): Result {
        val genres = genresRepository.getAll()
        return Result.GenresLoaded(genres)
    }

    private suspend fun searchMovies(query: String): Result {
        return try {
            val movies = repository.searchMovies(query)
            val mapped = viewEntityMapper.apply(movies)
            Result.Data(mapped)
        } catch (e: IOException) {
            Result.Error(e)
        }
    }

    private suspend fun storeInHistory(historyMovie: HistoryMovie): Result {
        val messageResId = when (historyMovie.rating) {
            0 -> R.string.will_less_like_this
            else -> R.string.will_more_like_this
        }
        historyRepository.store(historyMovie)

        val message = stringProvider.getString(messageResId)
        return Result.ShowSnackbar(message)
    }

    private fun navigate(recommendationsType: RecommendationsType) {
        _destinations.postValue(NavigationEvent(recommendationsType))
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
        navigationRelay.accept(category)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
