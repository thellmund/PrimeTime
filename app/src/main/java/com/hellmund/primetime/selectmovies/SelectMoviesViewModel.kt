package com.hellmund.primetime.selectmovies

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.hellmund.primetime.model2.Sample
import com.hellmund.primetime.utils.GenresProvider
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

data class SelectMoviesViewState(
        val data: List<Sample> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null
) {

    val isError: Boolean
        get() = error != null

}

sealed class Action {
    object Refresh : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<Sample>) : Result()
    data class Error(val error: Throwable) : Result()
}

class SelectMoviesViewModel(
        private val repository: SelectMoviesRepository,
        private val genresProvider: GenresProvider
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<SelectMoviesViewState>()
    val viewState: LiveData<SelectMoviesViewState> = _viewState

    init {
        val initialViewState = SelectMoviesViewState(isLoading = true)
        compositeDisposable += refreshRelay
                .switchMap(this::processAction)
                .scan(initialViewState, this::reduceState)
                .subscribe(this::render)
        refreshRelay.accept(Action.Refresh)
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            Action.Refresh -> fetchMovies()
        }
    }

    private fun fetchMovies(): Observable<Result> {
        return repository.fetch(genresProvider.getPreferredGenres())
                .subscribeOn(Schedulers.io())
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
                .startWith(Result.Loading)
    }

    private fun reduceState(
            viewState: SelectMoviesViewState,
            result: Result
    ): SelectMoviesViewState {
        return when (result) {
            Result.Loading -> viewState.copy(isLoading = true, error = null)
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
        }
    }

    private fun render(viewState: SelectMoviesViewState) {
        _viewState.postValue(viewState)
    }

    fun refresh() {
        refreshRelay.accept(Action.Refresh)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    class Factory(
            private val repository: SelectMoviesRepository,
            private val genresProvider: GenresProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectMoviesViewModel(repository, genresProvider) as T
        }

    }

}
