package com.hellmund.primetime.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

data class WatchlistViewState(
        val data: List<WatchlistMovieViewEntity> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null
)

sealed class Action {
    object Load : Action()
    data class DatabaseLoaded(val data: List<WatchlistMovieViewEntity>) : Action()
    data class Remove(val movie: WatchlistMovieViewEntity) : Action()
    data class Rated(val movie: WatchlistMovieViewEntity, val rating: Int) : Action()
}

sealed class Result {
    data class Data(val data: List<WatchlistMovieViewEntity>) : Result()
    data class Error(val error: Throwable) : Result()
    data class Removed(val movie: WatchlistMovieViewEntity) : Result()
}

class WatchlistViewModel @Inject constructor(
        private val repository: WatchlistRepository,
        private val historyRepository: HistoryRepository,
        private val viewEntityMapper: WatchlistMovieViewEntityMapper
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<WatchlistViewState>()
    val viewState: LiveData<WatchlistViewState> = _viewState

    init {
        val initialViewState = WatchlistViewState(isLoading = true)

        val databaseChanges = repository.getAll()
                .onErrorReturn { emptyList() }
                .map(viewEntityMapper)
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
            is Action.Rated -> rateMovie(action.movie, action.rating)
        }
    }

    private fun fetchMovies(): Observable<Result> {
        return repository.getAll()
                .subscribeOn(Schedulers.io())
                .map(viewEntityMapper)
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
                .toObservable()
    }

    private fun removeMovie(movie: WatchlistMovieViewEntity): Observable<Result> {
        return repository.remove(movie.id)
                .andThen(Observable.just(Result.Removed(movie) as Result))
    }

    private fun rateMovie(movie: WatchlistMovieViewEntity, rating: Int): Observable<Result> {
        val historyMovie = HistoryMovie.fromWatchlistMovie(movie, rating)
        return repository.remove(movie.id)
                .andThen {
                    historyRepository.store(historyMovie)
                    Completable.complete()
                }
                .andThen(Observable.just(Result.Removed(movie) as Result))
    }

    private fun reduceState(
            viewState: WatchlistViewState,
            result: Result
    ): WatchlistViewState {
        return when (result) {
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
            is Result.Removed -> viewState.copy(data = viewState.data.minus(result.movie))
        }
    }

    fun remove(movie: WatchlistMovieViewEntity) {
        refreshRelay.accept(Action.Remove(movie))
    }

    fun onMovieRated(movie: WatchlistMovieViewEntity, rating: Int) {
        refreshRelay.accept(Action.Rated(movie, rating))
    }

    private fun render(viewState: WatchlistViewState) {
        _viewState.postValue(viewState)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
