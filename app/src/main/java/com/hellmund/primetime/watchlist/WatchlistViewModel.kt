package com.hellmund.primetime.watchlist

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.hellmund.primetime.database.WatchlistMovie
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

data class WatchlistViewState(
        val data: List<WatchlistMovie> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null
)

sealed class Action {
    object Load : Action()
    data class DatabaseLoaded(val data: List<WatchlistMovie>) : Action()
}

sealed class Result {
    data class Data(val data: List<WatchlistMovie>) : Result()
    data class Error(val error: Throwable) : Result()
}

class WatchlistViewModel @Inject constructor(
        private val repository: WatchlistRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<WatchlistViewState>()
    val viewState: LiveData<WatchlistViewState> = _viewState

    init {
        val initialViewState = WatchlistViewState(isLoading = true)

        val databaseChanges = repository.getAll()
                .onErrorReturn { emptyList() }
                .toObservable()
                .map { if (it.isNotEmpty()) { Action.DatabaseLoaded(it) } else { Action.Load } }

        val sources = Observable.merge(refreshRelay, databaseChanges)

        compositeDisposable += sources
                .switchMap(this::processAction)
                .scan(initialViewState, this::reduceState)
                .subscribe(this::render)
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.Load -> fetchMovies()
            is Action.DatabaseLoaded -> Observable.just(Result.Data(action.data))
        }
    }

    private fun fetchMovies(): Observable<Result> {
        return repository.getAll()
                .subscribeOn(Schedulers.io())
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
                .toObservable()
    }

    private fun reduceState(
            viewState: WatchlistViewState,
            result: Result
    ): WatchlistViewState {
        return when (result) {
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
        }
    }

    fun remove(movie: WatchlistMovie) {
        repository.remove(movie).blockingAwait()
    }

    private fun render(viewState: WatchlistViewState) {
        _viewState.postValue(viewState)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
