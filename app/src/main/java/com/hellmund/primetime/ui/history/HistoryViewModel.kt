package com.hellmund.primetime.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

data class HistoryViewState(
        val data: List<HistoryMovieViewEntity> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null
)

sealed class Action {
    object Load : Action()
    data class DatabaseLoaded(val data: List<HistoryMovieViewEntity>) : Action()
    data class Remove(val movie: HistoryMovieViewEntity) : Action()
    data class Update(val movie: HistoryMovie) : Action()
}

sealed class Result {
    data class Data(val data: List<HistoryMovieViewEntity>) : Result()
    data class Error(val error: Throwable) : Result()
    data class Removed(val movie: HistoryMovieViewEntity) : Result()
    data class Updated(val movie: HistoryMovieViewEntity) : Result()
}

class HistoryViewModel @Inject constructor(
        private val repository: HistoryRepository,
        private val viewEntitiesMapper: HistoryMoviesViewEntityMapper,
        private val viewEntityMapper: HistoryMovieViewEntityMapper
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<HistoryViewState>()
    val viewState: LiveData<HistoryViewState> = _viewState

    init {
        val initialViewState = HistoryViewState(isLoading = true)

        val databaseChanges = repository.getAll()
                .map(viewEntitiesMapper)
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
            is Action.Remove -> removeMovie(action.movie)
            is Action.Update -> updateMovie(action.movie)
        }
    }

    private fun fetchMovies(): Observable<Result> {
        return repository.getAll()
                .subscribeOn(Schedulers.io())
                .map(viewEntitiesMapper)
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
                .toObservable()
    }

    private fun removeMovie(movie: HistoryMovieViewEntity): Observable<Result> {
        return repository.remove(movie.id)
                .andThen(Observable.just(Result.Removed(movie) as Result))
    }

    private fun updateMovie(movie: HistoryMovie): Observable<Result> {
        return repository
                .store(movie)
                .andThen(Observable.just(movie))
                .map(viewEntityMapper)
                .map { Result.Updated(it) }
    }

    private fun reduceState(
            viewState: HistoryViewState,
            result: Result
    ): HistoryViewState {
        return when (result) {
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
            is Result.Removed -> viewState.copy(data = viewState.data.minus(result.movie))
            is Result.Updated -> {
                val index = viewState.data.indexOfFirst { it.id == result.movie.id }
                val newData = viewState.data
                        .toMutableList()
                        .apply { set(index, result.movie) }
                        .toList()
                viewState.copy(data = newData)
            }
        }
    }

    private fun render(viewState: HistoryViewState) {
        _viewState.postValue(viewState)
    }

    fun update(movie: HistoryMovieViewEntity, newRating: Int) {
        val raw = movie.raw.copy(rating = newRating)
        refreshRelay.accept(Action.Update(raw))
    }

    fun remove(movie: HistoryMovieViewEntity) {
        refreshRelay.accept(Action.Remove(movie))
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
