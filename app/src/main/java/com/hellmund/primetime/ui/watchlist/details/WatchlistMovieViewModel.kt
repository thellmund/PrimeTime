package com.hellmund.primetime.ui.watchlist.details

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.hellmund.primetime.ui.watchlist.WatchlistMovieViewEntity
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

data class WatchlistMovieViewState(
        val movie: WatchlistMovieViewEntity,
        val isError: Boolean = false,
        val showToast: Boolean = false,
        val showRemoveDialog: Boolean = false
)

sealed class Action {
    object ToggleNotification : Action()
    object RequestRemove : Action()
}

sealed class Result {
    data class NotificationToggled(val data: WatchlistMovieViewEntity) : Result()
    object Error : Result()
    object ShowRemoveDialog : Result()
    object HideRemoveDialog : Result()
}

sealed class MovieEvent {
    data class MarkWatched(val movie: WatchlistMovieViewEntity) : MovieEvent()
    data class Remove(val movie: WatchlistMovieViewEntity) : MovieEvent()
}

class MovieStore @Inject constructor(var movie: WatchlistMovieViewEntity)

class WatchlistMovieViewModel @Inject constructor(
        private val store: MovieStore
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<WatchlistMovieViewState>()
    val viewState: LiveData<WatchlistMovieViewState> = _viewState

    private val _movieEvents = MutableLiveData<MovieEvent>()
    val movieEvents: LiveData<MovieEvent> = _movieEvents

    init {
        val initialViewState = WatchlistMovieViewState(movie = store.movie)
        compositeDisposable += refreshRelay
                .switchMap(this::processAction)
                .scan(initialViewState, this::reduceState)
                .subscribe(this::render)
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.ToggleNotification -> toggleNotification(store.movie)
            is Action.RequestRemove -> Observable.just(Result.ShowRemoveDialog)
        }
    }

    private fun toggleNotification(movie: WatchlistMovieViewEntity): Observable<Result> {
        val isActivated = movie.notificationsActivated.not()
        store.movie = movie.copy(notificationsActivated = isActivated)
        return Observable.just(Result.NotificationToggled(store.movie))
    }

    private fun reduceState(
            viewState: WatchlistMovieViewState,
            result: Result
    ): WatchlistMovieViewState {
        return when (result) {
            is Result.NotificationToggled -> viewState.copy(movie = result.data)
            is Error -> viewState.copy(isError = true)
            is Result.ShowRemoveDialog -> viewState.copy(showRemoveDialog = true)
            is Result.HideRemoveDialog -> viewState.copy(showRemoveDialog = false)
            else -> viewState // TODO
        }
    }

    private fun render(viewState: WatchlistMovieViewState) {
        _viewState.postValue(viewState)
    }

    fun onNotificationClick() {
        refreshRelay.accept(Action.ToggleNotification)
    }

    fun onRemove() {
        refreshRelay.accept(Action.RequestRemove)
    }

    fun onConfirmRemove() {
        _movieEvents.postValue(MovieEvent.Remove(store.movie))
    }

    fun onWatched() {
        _movieEvents.postValue(MovieEvent.MarkWatched(store.movie))
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
