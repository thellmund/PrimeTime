package com.hellmund.primetime.ui.suggestions

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.ui.watchlist.WatchlistRepository
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

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
    data class AdditionalInformationLoaded(val movie: MovieViewEntity) : ViewModelEvent()
    object TrailerLoading : ViewModelEvent()
    data class TrailerLoaded(val url: String) : ViewModelEvent()
    data class ImdbLinkLoaded(val url: String) : ViewModelEvent()
    data class RatingStored(val rating: Rating) : ViewModelEvent()
    object AddedToWatchlist : ViewModelEvent()
    object RemovedFromWatchlist : ViewModelEvent()
    object ShowRemoveFromWatchlistDialog : ViewModelEvent()
    data class WatchStatus(val watchStatus: Movie.WatchStatus) : ViewModelEvent()
}

sealed class Rating(val movie: MovieViewEntity) {
    class Like(movie: MovieViewEntity) : Rating(movie)
    class Dislike(movie: MovieViewEntity) : Rating(movie)
}

class SuggestionsViewModel @Inject constructor(
        private val repository: MoviesRepository,
        private val historyRepository: HistoryRepository,
        private val watchlistRepository: WatchlistRepository,
        private val viewEntityMapper: MovieViewEntityMapper,
        private var movie: MovieViewEntity
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
                        Observable.just(ViewModelEvent.WatchStatus(Movie.WatchStatus.WATCHED))
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
                        Observable.just(ViewModelEvent.WatchStatus(Movie.WatchStatus.ON_WATCHLIST))
                    } else {
                        Observable.just(ViewModelEvent.WatchStatus(Movie.WatchStatus.NOT_WATCHED))
                    }
                }
    }

    private fun fetchInformation(): Observable<ViewModelEvent> {
        return repository
                .fetchMovie(movie.id)
                .map(viewEntityMapper)
                .doOnNext { movie = it }
                .map { ViewModelEvent.AdditionalInformationLoaded(it) }
    }

    private fun fetchTrailer(): Observable<ViewModelEvent> {
        return repository
                .fetchVideo(movie)
                .map { ViewModelEvent.TrailerLoaded(it) as ViewModelEvent }
                .startWith(ViewModelEvent.TrailerLoading)
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
                        // Already on watchlist
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

    private fun storeInWatchlist(movie: MovieViewEntity): Observable<ViewModelEvent> {
        return watchlistRepository
                .store(movie.raw)
                .subscribeOn(Schedulers.io())
                .andThen(Observable.just(ViewModelEvent.AddedToWatchlist as ViewModelEvent))
    }

    private fun onRemoveFromWatchlist(): Observable<ViewModelEvent> {
        return watchlistRepository
                .remove(movie.id)
                .andThen(Observable.just(ViewModelEvent.RemovedFromWatchlist as ViewModelEvent))
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

}
