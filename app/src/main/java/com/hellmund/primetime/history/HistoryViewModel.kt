package com.hellmund.primetime.history

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.hellmund.primetime.database.HistoryMovie
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

data class HistoryViewState(
        val data: List<HistoryMovie> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null
)

sealed class Action {
    object Load : Action()
    data class DatabaseLoaded(val data: List<HistoryMovie>) : Action()
}

sealed class Result {
    data class Data(val data: List<HistoryMovie>) : Result()
    data class Error(val error: Throwable) : Result()
}

class HistoryViewModel @Inject constructor(
        private val repository: HistoryRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<HistoryViewState>()
    val viewState: LiveData<HistoryViewState> = _viewState

    init {
        val initialViewState = HistoryViewState(isLoading = true)

        val databaseChanges = repository.getAll()
                .onErrorReturn { emptyList() }
                .toObservable()
                .map {
                    if (it.isNotEmpty()) { Action.DatabaseLoaded(it) } else { Action.Load }
                }

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
            viewState: HistoryViewState,
            result: Result
    ): HistoryViewState {
        return when (result) {
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
        }
    }

    private fun render(viewState: HistoryViewState) {
        _viewState.postValue(viewState)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    class Factory(
            private val repository: HistoryRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }

    }

}
