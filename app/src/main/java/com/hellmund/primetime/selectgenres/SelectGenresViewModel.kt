package com.hellmund.primetime.selectgenres

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.hellmund.primetime.model.Genre
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

data class SelectGenresViewState(
        val data: List<Genre> = emptyList(),
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
    data class Data(val data: List<Genre>) : Result()
    data class Error(val error: Throwable) : Result()
}

class SelectGenresViewModel(
        private val repository: GenresRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<SelectGenresViewState>()
    val viewState: LiveData<SelectGenresViewState> = _viewState

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
            Action.Refresh -> fetchMovies()
        }
    }

    private fun fetchMovies(): Observable<Result> {
        return repository.fetchGenres()
                .subscribeOn(Schedulers.io())
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
                .startWith(Result.Loading)
    }

    private fun reduceState(
            viewState: SelectGenresViewState,
            result: Result
    ): SelectGenresViewState {
        return when (result) {
            Result.Loading -> viewState.copy(isLoading = true, error = null)
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
        }
    }

    private fun render(viewState: SelectGenresViewState) {
        _viewState.postValue(viewState)
    }

    fun refresh() {
        refreshRelay.accept(Action.Refresh)
    }

    fun store(genres: Set<String>) {
        repository.storeGenres(genres)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
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
