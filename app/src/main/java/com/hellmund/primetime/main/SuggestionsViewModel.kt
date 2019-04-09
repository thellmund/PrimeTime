package com.hellmund.primetime.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.hellmund.primetime.database.HistoryMovie
import com.hellmund.primetime.history.HistoryRepository
import com.hellmund.primetime.model2.ApiMovie
import com.hellmund.primetime.utils.plusAssign
import com.hellmund.primetime.watchlist.WatchlistRepository
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

sealed class ViewModelAction {
    object LoadAdditionalInformation : ViewModelAction()
    object LoadTrailer : ViewModelAction()
    object OpenImdb : ViewModelAction()
    object LoadWatchStatus : ViewModelAction()
    data class StoreRating(val rating: Rating) : ViewModelAction()
    object AddToWatchlist : ViewModelAction()
    object RemoveFromWatchlist : ViewModelAction()
}

sealed class ViewModelEvent {
    data class AdditionalInformationLoaded(val movie: ApiMovie) : ViewModelEvent()
    object TrailerLoading : ViewModelEvent()
    data class TrailerLoaded(val url: String) : ViewModelEvent()
    data class ImdbLinkLoaded(val url: String) : ViewModelEvent()
    data class RatingStored(val rating: Rating) : ViewModelEvent()
    object AddedToWatchlist : ViewModelEvent()
    object RemovedFromWatchlist : ViewModelEvent()
    object ShowRemoveFromWatchlistDialog : ViewModelEvent()
    data class WatchStatus(val watchStatus: ApiMovie.WatchStatus) : ViewModelEvent()
}

sealed class Rating(val movie: ApiMovie) {
    class Like(movie: ApiMovie) : Rating(movie)
    class Dislike(movie: ApiMovie) : Rating(movie)
}

class SuggestionsViewModel(
        private val repository: RecommendationsRepository,
        private val historyRepository: HistoryRepository,
        private val watchlistRepository: WatchlistRepository,
        private var movie: ApiMovie
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val actionsRelay = PublishRelay.create<ViewModelAction>()

    private val _viewModelEvents = MutableLiveData<ViewModelEvent>()
    val viewModelEvents: LiveData<ViewModelEvent> = _viewModelEvents

    init {
        compositeDisposable += actionsRelay
                .switchMap(this::processAction)
                .subscribe(this::render)
        actionsRelay.accept(ViewModelAction.LoadWatchStatus)
    }

    private fun processAction(action: ViewModelAction): Observable<ViewModelEvent> {
        return when (action) {
            is ViewModelAction.LoadWatchStatus -> loadWatchStatus()
            is ViewModelAction.LoadAdditionalInformation -> fetchInformation()
            is ViewModelAction.LoadTrailer -> fetchTrailer()
            is ViewModelAction.OpenImdb -> fetchImdbLink()
            is ViewModelAction.StoreRating -> storeRating(action.rating)
            is ViewModelAction.AddToWatchlist -> onAddToWatchlist()
            is ViewModelAction.RemoveFromWatchlist -> onRemoveFromWatchlist()
        }
    }

    private fun loadWatchStatus(): Observable<ViewModelEvent> {
        return historyRepository.count(movie.id)
                .subscribeOn(Schedulers.io())
                .flatMapObservable {
                    if (it > 0) {
                        Observable.just(ViewModelEvent.WatchStatus(ApiMovie.WatchStatus.WATCHED))
                    } else {
                        fetchWatchlistStatus()
                    }
                }
    }

    private fun fetchWatchlistStatus(): Observable<ViewModelEvent> {
        return watchlistRepository
                .count(movie.id)
                .subscribeOn(Schedulers.io())
                .flatMapObservable {
                    if (it > 0) {
                        Observable.just(ViewModelEvent.WatchStatus(ApiMovie.WatchStatus.ON_WATCHLIST))
                    } else {
                        Observable.just(ViewModelEvent.WatchStatus(ApiMovie.WatchStatus.NOT_WATCHED))
                    }
                }
    }

    private fun fetchInformation(): Observable<ViewModelEvent> {
        return repository
                .fetchMovie(movie.id)
                .doOnNext {
                    movie = it
                }
                .map { ViewModelEvent.AdditionalInformationLoaded(it) }
    }

    private fun fetchTrailer(): Observable<ViewModelEvent> {
        return repository
                .fetchVideo(movie)
                .startWith { ViewModelEvent.TrailerLoading }
                .map { ViewModelEvent.TrailerLoaded(it) }
    }

    private fun fetchImdbLink(): Observable<ViewModelEvent> {
        val url = "http://www.imdb.com/title/${movie.imdbId}"
        return Observable.just(ViewModelEvent.ImdbLinkLoaded(url))
    }

    private fun storeRating(rating: Rating): Observable<ViewModelEvent> {
        val historyMovie = HistoryMovie.fromRating(rating)
        return Observable
                .fromCallable { historyRepository.store(historyMovie) }
                .subscribeOn(Schedulers.io())
                .map { ViewModelEvent.RatingStored(rating) }
    }

    private fun onAddToWatchlist(): Observable<ViewModelEvent> {
        return watchlistRepository
                .count(movie.id)
                .flatMapObservable {
                    if (it > 0) {
                        Observable.just(ViewModelEvent.ShowRemoveFromWatchlistDialog)
                    } else {
                        storeInWatchlist(movie)
                    }
                }
                /*.flatMapCompletable {
                    storeInWatchlist(movie)
                }
                .toObservable<Unit>()
                .map {
                    ViewModelEvent.AddedToWatchlist as ViewModelEvent
                }
                .onErrorResumeNext { t: Throwable ->
                    // Movie not in the watchlist
                    Observable.just(ViewModelEvent.ShowRemoveFromWatchlistDialog)
                }*/
                .subscribeOn(Schedulers.io())
    }

    private fun storeInWatchlist(movie: ApiMovie): Observable<ViewModelEvent> {
        return watchlistRepository
                .store(movie)
                .subscribeOn(Schedulers.io())
                .toObservable<Unit>()
                .map { ViewModelEvent.AddedToWatchlist }
    }

    private fun onRemoveFromWatchlist(): Observable<ViewModelEvent> {
        return watchlistRepository
                .remove(movie.id)
                .toObservable<Unit>()
                .map { ViewModelEvent.RemovedFromWatchlist }
    }

    fun loadTrailer() {
        actionsRelay.accept(ViewModelAction.LoadTrailer)
    }

    fun loadAdditionalInformation() {
        actionsRelay.accept(ViewModelAction.LoadAdditionalInformation)
    }

    fun openImdb() {
        actionsRelay.accept(ViewModelAction.OpenImdb)
    }

    fun handleRating(which: Int) {
        val rating = if (which == 0) Rating.Like(movie) else Rating.Dislike(movie)
        actionsRelay.accept(ViewModelAction.StoreRating(rating))
    }

    fun addToWatchlist() {
        actionsRelay.accept(ViewModelAction.AddToWatchlist)
    }

    fun removeFromWatchlist() {
        actionsRelay.accept(ViewModelAction.RemoveFromWatchlist)
    }

    private fun render(event: ViewModelEvent) {
        _viewModelEvents.postValue(event)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    class Factory(
            private val repository: RecommendationsRepository,
            private val historyRepository: HistoryRepository,
            private val watchlistRepository: WatchlistRepository,
            private val movie: ApiMovie
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SuggestionsViewModel(repository, historyRepository, watchlistRepository, movie) as T
        }

    }

}
