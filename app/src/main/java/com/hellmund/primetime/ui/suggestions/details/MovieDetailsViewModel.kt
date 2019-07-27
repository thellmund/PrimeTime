package com.hellmund.primetime.ui.suggestions.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectstreamingservices.StreamingService
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.MovieViewEntityMapper
import com.hellmund.primetime.ui.suggestions.MoviesViewEntityMapper
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.ui.watchlist.WatchlistRepository
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

sealed class ViewModelAction {
    object LoadStreamingServices : ViewModelAction()
    object LoadRecommendations : ViewModelAction()
    object LoadReviews : ViewModelAction()
    object LoadAdditionalInformation : ViewModelAction()
    object LoadTrailer : ViewModelAction()
    object OpenImdb : ViewModelAction()
    object LoadWatchStatus : ViewModelAction()
    data class StoreRating(val rating: Rating) : ViewModelAction()
    object AddToWatchlist : ViewModelAction()
    object RemoveFromWatchlist : ViewModelAction()
    data class LoadColorPalette(val bitmap: Bitmap) : ViewModelAction()
}

sealed class ViewModelEvent {
    data class StreamingServicesLoaded(val services: List<StreamingService>) : ViewModelEvent()
    data class RecommendationsLoaded(val movies: List<MovieViewEntity>) : ViewModelEvent()
    data class ReviewsLoaded(val reviews: List<Review>) : ViewModelEvent()
    data class AdditionalInformationLoaded(val movie: MovieViewEntity) : ViewModelEvent()
    object TrailerLoading : ViewModelEvent()
    data class TrailerLoaded(val url: String) : ViewModelEvent()
    data class ImdbLinkLoaded(val url: String) : ViewModelEvent()
    data class RatingStored(val rating: Rating) : ViewModelEvent()
    object AddedToWatchlist : ViewModelEvent()
    object RemovedFromWatchlist : ViewModelEvent()
    data class WatchStatus(val watchStatus: Movie.WatchStatus) : ViewModelEvent()
    data class ColorPaletteLoaded(val palette: Palette) : ViewModelEvent()
    object None : ViewModelEvent()
}

sealed class Rating(val movie: MovieViewEntity) {
    class Like(movie: MovieViewEntity) : Rating(movie)
    class Dislike(movie: MovieViewEntity) : Rating(movie)
}

class MovieDetailsViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val historyRepository: HistoryRepository,
    private val watchlistRepository: WatchlistRepository,
    private val viewEntitiesMapper: MoviesViewEntityMapper,
    private val viewEntityMapper: MovieViewEntityMapper,
    private var movie: MovieViewEntity
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val actionsRelay = PublishRelay.create<ViewModelAction>()

    private val _viewModelEvents = MutableLiveData<ViewModelEvent>()
    val viewModelEvents: LiveData<ViewModelEvent> = _viewModelEvents

    init {
        compositeDisposable += actionsRelay
            .flatMap(this::processAction)
            .subscribe(this::render)
        actionsRelay.accept(ViewModelAction.LoadStreamingServices)
    }

    private fun processAction(action: ViewModelAction): Observable<ViewModelEvent> {
        return when (action) {
            is ViewModelAction.LoadStreamingServices -> fetchStreamingServices()
            is ViewModelAction.LoadRecommendations -> fetchRecommendations()
            is ViewModelAction.LoadReviews -> fetchReviews()
            is ViewModelAction.LoadWatchStatus -> fetchWatchStatus()
            is ViewModelAction.LoadAdditionalInformation -> fetchInformation()
            is ViewModelAction.LoadTrailer -> fetchTrailer()
            is ViewModelAction.OpenImdb -> fetchImdbLink()
            is ViewModelAction.StoreRating -> storeRating(action.rating)
            is ViewModelAction.AddToWatchlist -> onAddToWatchlist()
            is ViewModelAction.RemoveFromWatchlist -> onRemoveFromWatchlist()
            is ViewModelAction.LoadColorPalette -> onLoadColorPalette(action.bitmap)
        }
    }

    private fun fetchStreamingServices(): Observable<ViewModelEvent> {
        return Observable
            .just(listOf(StreamingService("iTunes", true), StreamingService("Netflix", true)))
            .map { ViewModelEvent.StreamingServicesLoaded(it) }
    }

    private fun fetchRecommendations(): Observable<ViewModelEvent> {
        return repository
            .fetchRecommendations(movie.id)
            .map(viewEntitiesMapper)
            .map { ViewModelEvent.RecommendationsLoaded(it) as ViewModelEvent }
            .onErrorReturnItem(ViewModelEvent.None)
    }

    private fun fetchReviews(): Observable<ViewModelEvent> {
        return repository
            .fetchReviews(movie.id)
            .map {
                ViewModelEvent.ReviewsLoaded(it) as ViewModelEvent
            }
            .onErrorReturnItem(ViewModelEvent.None)
    }

    private fun fetchWatchStatus(): Observable<ViewModelEvent> {
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
        return historyRepository
            .store(historyMovie)
            .subscribeOn(Schedulers.io())
            .andThen(Observable.just(ViewModelEvent.RatingStored(rating) as ViewModelEvent))
    }

    private fun onAddToWatchlist(): Observable<ViewModelEvent> {
        return watchlistRepository
            .count(movie.id)
            .flatMapObservable {
                if (it > 0) {
                    // Already on watchlist
                    Observable.just(ViewModelEvent.RemovedFromWatchlist)
                } else {
                    storeInWatchlist(movie)
                }
            }
    }

    private fun storeInWatchlist(movie: MovieViewEntity): Observable<ViewModelEvent> {
        return repository
            .fetchMovie(movie.id)
            .flatMapCompletable { watchlistRepository.store(it) }
            .subscribeOn(Schedulers.io())
            .andThen(Observable.just(ViewModelEvent.AddedToWatchlist as ViewModelEvent))
    }

    private fun onRemoveFromWatchlist(): Observable<ViewModelEvent> {
        return watchlistRepository
            .remove(movie.id)
            .andThen(Observable.just(ViewModelEvent.RemovedFromWatchlist as ViewModelEvent))
    }

    private fun onLoadColorPalette(bitmap: Bitmap): Observable<ViewModelEvent> {
        return Observable
            .fromCallable { Palette.from(bitmap).generate() }
            .subscribeOn(Schedulers.io())
            .map { ViewModelEvent.ColorPaletteLoaded(it) as ViewModelEvent }
            .onErrorReturnItem(ViewModelEvent.None)
    }

    fun loadTrailer() {
        actionsRelay.accept(ViewModelAction.LoadTrailer)
    }

    fun loadAdditionalInformation() {
        actionsRelay.accept(ViewModelAction.LoadAdditionalInformation)
    }

    fun loadRecommendations() {
        actionsRelay.accept(ViewModelAction.LoadRecommendations)
    }

    fun loadReviews() {
        actionsRelay.accept(ViewModelAction.LoadReviews)
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

    fun loadColorPalette(bitmap: Bitmap) {
        actionsRelay.accept(ViewModelAction.LoadColorPalette(bitmap))
    }

    private fun render(event: ViewModelEvent) {
        _viewModelEvents.postValue(event)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
