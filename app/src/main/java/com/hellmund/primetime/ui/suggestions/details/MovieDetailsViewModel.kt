package com.hellmund.primetime.ui.suggestions.details

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectstreamingservices.StreamingService
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.MovieViewEntityMapper
import com.hellmund.primetime.ui.suggestions.MoviesViewEntityMapper
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.ui.watchlist.WatchlistRepository
import com.hellmund.primetime.utils.observe
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

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
    object None : ViewModelEvent()
}

class ViewModelEventStore {

    val viewState = MutableLiveData<ViewModelEvent>()

    fun observe(
        owner: LifecycleOwner,
        observer: (ViewModelEvent) -> Unit
    ) = viewState.observe(owner) { observer(it) }

    fun dispatch(
        result: ViewModelEvent
    ) {
        viewState.value = result
    }

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

    private val store = ViewModelEventStore()
    val viewModelEvents: LiveData<ViewModelEvent> = store.viewState

    init {
        viewModelScope.launch {
            store.dispatch(fetchWatchStatus())
        }
    }

    private suspend fun fetchRecommendations(): ViewModelEvent {
        return try {
            val movies = repository.fetchRecommendations(movie.id)
            val mapped = viewEntitiesMapper.apply(movies)
            ViewModelEvent.RecommendationsLoaded(mapped)
        } catch (e: IOException) {
            ViewModelEvent.None
        }
    }

    private suspend fun fetchReviews(): ViewModelEvent {
        return try {
            val reviews = repository.fetchReviews(movie.id)
            ViewModelEvent.ReviewsLoaded(reviews)
        } catch (e: IOException) {
            ViewModelEvent.None
        }
    }

    private suspend fun fetchWatchStatus(): ViewModelEvent {
        val count = historyRepository.count(movie.id)
        return if (count > 0) {
            ViewModelEvent.WatchStatus(Movie.WatchStatus.WATCHED)
        } else {
            fetchWatchlistStatus()
        }
    }

    private suspend fun fetchWatchlistStatus(): ViewModelEvent {
        val count = watchlistRepository.count(movie.id)
        return if (count > 0) {
            ViewModelEvent.WatchStatus(Movie.WatchStatus.ON_WATCHLIST)
        } else {
            ViewModelEvent.WatchStatus(Movie.WatchStatus.NOT_WATCHED)
        }
    }

    private suspend fun fetchInformation(): ViewModelEvent {
        val movie = repository.fetchMovie(movie.id)
        val viewEntity = viewEntityMapper.apply(movie)
        return ViewModelEvent.AdditionalInformationLoaded(viewEntity)
    }

    private suspend fun fetchTrailer(): ViewModelEvent {
        val video = repository.fetchVideo(movie)
        return ViewModelEvent.TrailerLoaded(video)
    }

    private fun fetchImdbLink(): ViewModelEvent {
        val url = "http://www.imdb.com/title/${movie.imdbId}"
        return ViewModelEvent.ImdbLinkLoaded(url)
    }

    /*private suspend fun storeRating(rating: Rating): ViewModelEvent {
        val historyMovie = HistoryMovie.fromRating(rating)
        historyRepository.store(historyMovie)
        return ViewModelEvent.RatingStored(rating)
    }*/

    private suspend fun onAddToWatchlist(): ViewModelEvent {
        val count = watchlistRepository.count(movie.id)
        return if (count > 0) {
            ViewModelEvent.RemovedFromWatchlist
        } else {
            storeInWatchlist(movie)
        }
    }

    private suspend fun storeInWatchlist(movie: MovieViewEntity): ViewModelEvent {
        val fetchedMovie = repository.fetchMovie(movie.id)
        watchlistRepository.store(fetchedMovie)
        return ViewModelEvent.AddedToWatchlist
    }

    private suspend fun onRemoveFromWatchlist(): ViewModelEvent {
        watchlistRepository.remove(movie.id)
        return ViewModelEvent.RemovedFromWatchlist
    }

    fun loadTrailer() {
        viewModelScope.launch {
            store.dispatch(ViewModelEvent.TrailerLoading)
            store.dispatch(fetchTrailer())
        }
    }

    fun loadAdditionalInformation() {
        viewModelScope.launch {
            store.dispatch(fetchInformation())
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            store.dispatch(fetchRecommendations())
        }
    }

    fun loadReviews() {
        viewModelScope.launch {
            store.dispatch(fetchReviews())
        }
    }

    fun openImdb() {
        store.dispatch(fetchImdbLink())
    }

    /*fun handleRating(which: Int) {
        viewModelScope.launch {
            val rating = if (which == 0) Rating.Like(movie) else Rating.Dislike(movie)
            store.dispatch(storeRating(rating))
        }
    }*/

    fun addToWatchlist() {
        viewModelScope.launch {
            store.dispatch(onAddToWatchlist())
        }
    }

    fun removeFromWatchlist() {
        viewModelScope.launch {
            store.dispatch(onRemoveFromWatchlist())
        }
    }

}
