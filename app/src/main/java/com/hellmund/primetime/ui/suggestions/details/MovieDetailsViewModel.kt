package com.hellmund.primetime.ui.suggestions.details

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.hellmund.api.Review
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.HistoryRepository
import com.hellmund.primetime.ui.suggestions.MovieViewEntitiesMapper
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.MovieViewEntityMapper
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.ui.watchlist.WatchlistRepository
import com.hellmund.primetime.ui_common.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

sealed class Action {
    object AddToWatchlist : Action()
    data class LoadColorPalette(val bitmap: Bitmap) : Action()
    object OpenImdb : Action()
    object OpenTrailer : Action()
    object RemoveFromWatchlist : Action()
}

sealed class UiEvent {
    data class RecommendationsLoaded(val movies: List<MovieViewEntity>) : UiEvent()
    data class ReviewsLoaded(val reviews: List<Review>) : UiEvent()
    data class AdditionalInformationLoaded(val movie: MovieViewEntity) : UiEvent()
    object TrailerLoading : UiEvent()
    data class TrailerLoaded(val url: String) : UiEvent()
    data class ImdbLinkLoaded(val url: String) : UiEvent()
    object AddedToWatchlist : UiEvent()
    object RemovedFromWatchlist : UiEvent()
    data class WatchStatus(val watchStatus: Movie.WatchStatus) : UiEvent()
    data class ColorPaletteLoaded(val palette: Palette) : UiEvent()
    object None : UiEvent()
}

class UiEventStore {

    val viewState = MutableLiveData<UiEvent>()

    fun observe(
        owner: LifecycleOwner,
        observer: (UiEvent) -> Unit
    ) = viewState.observe(owner) { observer(it) }

    fun dispatch(
        result: UiEvent
    ) {
        viewState.value = result
    }

}

class MovieDetailsViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val historyRepository: HistoryRepository,
    private val watchlistRepository: WatchlistRepository,
    private val viewEntitiesMapper: MovieViewEntitiesMapper,
    private val viewEntityMapper: MovieViewEntityMapper,
    private var movie: MovieViewEntity
) : ViewModel() {

    private val store = UiEventStore()
    val uiEvents: LiveData<UiEvent> = store.viewState

    init {
        viewModelScope.launch {
            store.dispatch(fetchWatchStatus())
            store.dispatch(fetchInformation())
            store.dispatch(fetchRecommendations())
            store.dispatch(fetchReviews())
        }
    }

    private suspend fun fetchRecommendations(): UiEvent {
        return try {
            val movies = repository.fetchRecommendations(movie.id)
            val mapped = viewEntitiesMapper(movies)
            UiEvent.RecommendationsLoaded(mapped)
        } catch (e: IOException) {
            UiEvent.None
        }
    }

    private suspend fun fetchReviews(): UiEvent {
        return try {
            val reviews = repository.fetchReviews(movie.id)
            UiEvent.ReviewsLoaded(reviews)
        } catch (e: IOException) {
            UiEvent.None
        }
    }

    private suspend fun fetchWatchStatus(): UiEvent {
        val count = historyRepository.count(movie.id)
        return if (count > 0) {
            UiEvent.WatchStatus(Movie.WatchStatus.WATCHED)
        } else {
            fetchWatchlistStatus()
        }
    }

    private suspend fun fetchWatchlistStatus(): UiEvent {
        val count = watchlistRepository.count(movie.id)
        return if (count > 0) {
            UiEvent.WatchStatus(Movie.WatchStatus.ON_WATCHLIST)
        } else {
            UiEvent.WatchStatus(Movie.WatchStatus.NOT_WATCHED)
        }
    }

    private suspend fun fetchInformation(): UiEvent {
        val movie = repository.fetchMovie(movie.id)
        val viewEntity = viewEntityMapper(movie)
        return UiEvent.AdditionalInformationLoaded(viewEntity)
    }

    private suspend fun fetchTrailer(): UiEvent {
        val video = repository.fetchVideo(movie)
        return UiEvent.TrailerLoaded(video)
    }

    private suspend fun onAddToWatchlist(): UiEvent {
        val count = watchlistRepository.count(movie.id)
        return if (count > 0) {
            UiEvent.RemovedFromWatchlist
        } else {
            storeInWatchlist(movie)
        }
    }

    private suspend fun storeInWatchlist(movie: MovieViewEntity): UiEvent {
        val fetchedMovie = repository.fetchMovie(movie.id)
        watchlistRepository.store(fetchedMovie)
        return UiEvent.AddedToWatchlist
    }

    private suspend fun onRemoveFromWatchlist(): UiEvent {
        watchlistRepository.remove(movie.id)
        return UiEvent.RemovedFromWatchlist
    }

    private suspend fun loadTrailer() {
        store.dispatch(UiEvent.TrailerLoading)
        store.dispatch(fetchTrailer())
    }

    private suspend fun loadImdbId() {
        val imdbId = movie.imdbId ?: repository.fetchMovie(movie.id).imdbId
        imdbId?.let {
            val link = "http://www.imdb.com/title/$it"
            store.dispatch(UiEvent.ImdbLinkLoaded(link))
        }
    }

    private suspend fun addToWatchlist() {
        store.dispatch(onAddToWatchlist())
    }

    private suspend fun removeFromWatchlist() {
        store.dispatch(onRemoveFromWatchlist())
    }

    private suspend fun loadColorPalette(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val event = try {
                val palette = Palette.from(bitmap).generate()
                UiEvent.ColorPaletteLoaded(palette)
            } catch (e: Exception) {
                UiEvent.None
            }
            withContext(Dispatchers.Main) {
                store.dispatch(event)
            }
        }
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.AddToWatchlist -> addToWatchlist()
                is Action.LoadColorPalette -> loadColorPalette(action.bitmap)
                is Action.OpenImdb -> loadImdbId()
                is Action.OpenTrailer -> loadTrailer()
                is Action.RemoveFromWatchlist -> removeFromWatchlist()
            }
        }
    }

}
