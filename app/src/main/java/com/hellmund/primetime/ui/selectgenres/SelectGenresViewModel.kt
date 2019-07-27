package com.hellmund.primetime.ui.selectgenres

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.shared.SingleLiveDataEvent
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

data class SelectGenresViewState(
    val data: List<Genre> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null
)

sealed class Action {
    object Refresh : Action()
    data class ToggleGenre(val genre: Genre) : Action()
    data class Store(val genres: List<Genre>) : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<Genre>) : Result()
    data class Error(val error: Throwable) : Result()
    data class GenreToggled(val genre: Genre) : Result()
    object None : Result()
}

class SelectGenresViewModel @Inject constructor(
    private val repository: GenresRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<SelectGenresViewState>()
    val viewState: LiveData<SelectGenresViewState> = _viewState

    private val _navigation = MutableLiveData<SingleLiveDataEvent<Unit>>()
    val navigation: LiveData<SingleLiveDataEvent<Unit>> = _navigation

    init {
        val initialViewState = SelectGenresViewState(isLoading = true)
        compositeDisposable += refreshRelay
            .switchMap(this::processAction)
            .scan(initialViewState, this::reduceState)
            .subscribe(this::render)
        refreshRelay.accept(Action.Refresh)
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.Refresh -> fetchGenres()
            is Action.ToggleGenre -> toggleGenre(action.genre)
            is Action.Store -> storeGenres(action.genres)
        }
    }

    private fun fetchGenres(): Observable<Result> {
        return repository.fetchGenres()
            .subscribeOn(Schedulers.io())
            .map { Result.Data(it) as Result }
            .onErrorReturn { Result.Error(it) }
            .startWith(Result.Loading)
    }

    private fun toggleGenre(genre: Genre): Observable<Result> {
        val newGenre = genre.copy(isPreferred = !genre.isPreferred)
        return Observable.just(Result.GenreToggled(newGenre))
    }

    private fun storeGenres(genres: List<Genre>): Observable<Result> {
        return repository
            .storeGenres(genres)
            .andThen(Observable.just(Result.None as Result))
            .doOnNext { _navigation.postValue(SingleLiveDataEvent(Unit)) }
    }

    private fun reduceState(
        viewState: SelectGenresViewState,
        result: Result
    ): SelectGenresViewState {
        return when (result) {
            is Result.Loading -> viewState.copy(isLoading = true, error = null)
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
            is Result.GenreToggled -> {
                val index = viewState.data.indexOfFirst { it.id == result.genre.id }
                val newData = viewState.data.replace(index, result.genre)
                viewState.copy(data = newData)
            }
            is Result.None -> viewState
        }
    }

    private fun render(viewState: SelectGenresViewState) {
        _viewState.postValue(viewState)
    }

    fun store(genres: List<Genre>) {
        refreshRelay.accept(Action.Store(genres))
    }

    fun onGenreToggled(genre: Genre) {
        refreshRelay.accept(Action.ToggleGenre(genre))
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    private fun <T> List<T>.replace(index: Int, element: T): List<T> {
        return toMutableList().apply {
            removeAt(index)
            add(index, element)
        }
    }

    class Factory(
        private val repository: GenresRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectGenresViewModel(repository) as T
        }

    }

}
