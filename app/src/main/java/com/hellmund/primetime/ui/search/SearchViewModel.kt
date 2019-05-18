package com.hellmund.primetime.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.MoviesViewEntityMapper
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.utils.StringProvider
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
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

sealed class Action {
    object LoadGenres : Action()
    data class Typed(val input: String) : Action()
    data class Search(val query: String) : Action()
    data class AddToHistory(val historyMovie: HistoryMovie) : Action()
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

class SearchViewModel @Inject constructor(
        private val repository: MoviesRepository,
        private val historyRepository: HistoryRepository,
        private val genresRepository: GenresRepository,
        private val viewEntityMapper: MoviesViewEntityMapper,
        private val stringProvider: StringProvider
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val actionsRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<SearchViewState>()
    val viewState: LiveData<SearchViewState> = _viewState

    init {
        val initialViewState = SearchViewState()
        compositeDisposable += actionsRelay
                .switchMap(this::processAction)
                .scan(initialViewState, this::reduceState)
                .subscribe(this::render)
        actionsRelay.accept(Action.LoadGenres)
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.LoadGenres -> fetchGenres()
            is Action.Typed -> onTyped(action.input)
            is Action.Search -> searchMovies(action.query)
            is Action.AddToHistory -> storeInHistory(action.historyMovie)
        }
    }

    private fun fetchGenres(): Observable<Result> {
        return genresRepository.all
                .map { Result.GenresLoaded(it) as Result }
                .toObservable()
    }

    private fun onTyped(input: String): Observable<Result> {
        return Observable.just(Result.ToggleClearButton(input.isNotEmpty()))
    }

    private fun searchMovies(query: String): Observable<Result> {
        return repository.searchMovies(query)
                .map(viewEntityMapper)
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
    }

    private fun storeInHistory(historyMovie: HistoryMovie): Observable<Result> {
        val messageResId = when (historyMovie.rating) {
            0 -> R.string.will_less_like_this
            else -> R.string.will_more_like_this
        }
        return historyRepository
                .store(historyMovie)
                .subscribeOn(Schedulers.io())
                .andThen(createSelfDismissingSnackbar(messageResId))
    }

    private fun createSelfDismissingSnackbar(resId: Int): Observable<Result> {
        val message = stringProvider.getString(resId)
        return Observable.timer(4, TimeUnit.SECONDS)
                .map { Result.DismissSnackbar as Result }
                .startWith(Result.ShowSnackbar(message))
    }

    private fun reduceState(
            viewState: SearchViewState,
            result: Result
    ): SearchViewState {
        return when (result) {
            is Result.GenresLoaded -> viewState.copy(genres = result.genres)
            is Result.Loading -> viewState.copy(isLoading = true, error = null)
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null, didPerformSearch = true)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error, didPerformSearch = true)
            is Result.ToggleClearButton -> viewState.copy(showClearButton = result.show)
            is Result.ShowSnackbar -> viewState.copy(snackbarText = result.message)
            is Result.DismissSnackbar -> viewState.copy(snackbarText = null)
        }
    }

    private fun render(viewState: SearchViewState) {
        _viewState.postValue(viewState)
    }

    fun search(query: String) {
        actionsRelay.accept(Action.Search(query))
    }

    fun onTextChanged(input: String) {
        actionsRelay.accept(Action.Typed(input))
    }

    fun addToHistory(historyMovie: HistoryMovie) {
        actionsRelay.accept(Action.AddToHistory(historyMovie))
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
