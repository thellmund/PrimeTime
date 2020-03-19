package com.hellmund.primetime.moviedetails.ui

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.hellmund.api.model.Review
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.data.repositories.WatchlistRepository
import com.hellmund.primetime.moviedetails.data.MovieDetailsRepository
import com.hellmund.primetime.ui_common.MovieViewEntitiesMapper
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.viewmodel.Event
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.SingleEventStore
import com.hellmund.primetime.ui_common.viewmodel.viewStateStore
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ViewEvent {
    object AddToWatchlist : ViewEvent()
    data class LoadColorPalette(val bitmap: Bitmap) : ViewEvent()
    object OpenImdb : ViewEvent()
    object OpenTrailer : ViewEvent()
    object RemoveFromWatchlist : ViewEvent()
}

sealed class ViewResult {
    data class MovieLoaded(val movie: MovieViewEntity.Full) : ViewResult()
    data class RecommendationsLoaded(val movies: List<MovieViewEntity.Partial>) : ViewResult()
    data class ReviewsLoaded(val reviews: List<Review>) : ViewResult()
    object TrailerLoading : ViewResult()
    object TrailerLoaded : ViewResult()
    data class LoadedWatchStatus(val watchStatus: Movie.WatchStatus) : ViewResult()
    data class ColorPaletteLoaded(val palette: Palette) : ViewResult()
    object None : ViewResult()
}

sealed class ViewEffect {
    data class OpenTrailer(val url: String) : ViewEffect()
    data class OpenImdb(val url: String) : ViewEffect()
}

data class MovieDetailsViewState(
    val movie: MovieViewEntity,
    val isLoading: Boolean = true,
    val recommendations: List<MovieViewEntity.Partial>? = null,
    val reviews: List<Review>? = null,
    val watchStatus: Movie.WatchStatus = Movie.WatchStatus.NOT_WATCHED,
    val color: Int? = null,
    val isTrailerLoading: Boolean = false
)

class MovieDetailsViewStateReducer : Reducer<MovieDetailsViewState, ViewResult> {
    override fun invoke(
        state: MovieDetailsViewState,
        viewResult: ViewResult
    ) = when (viewResult) {
        is ViewResult.MovieLoaded -> state.copy(movie = viewResult.movie, isLoading = false)
        is ViewResult.RecommendationsLoaded -> state.copy(recommendations = viewResult.movies)
        is ViewResult.TrailerLoading -> state.copy(isTrailerLoading = true)
        is ViewResult.TrailerLoaded -> state.copy(isTrailerLoading = false)
        is ViewResult.ReviewsLoaded -> state.copy(reviews = viewResult.reviews)
        is ViewResult.LoadedWatchStatus -> state.copy(watchStatus = viewResult.watchStatus)
        is ViewResult.ColorPaletteLoaded -> state.copy(color = viewResult.palette.mutedSwatch?.rgb)
        is ViewResult.None -> state
    }
}

class MovieDetailsViewModel @Inject constructor(
    private val repository: MovieDetailsRepository,
    private val historyRepository: HistoryRepository,
    private val watchlistRepository: WatchlistRepository,
    private val viewEntitiesMapper: MovieViewEntitiesMapper,
    private val movie: MovieViewEntity
) : ViewModel() {

    private val store = viewStateStore(
        initialState = MovieDetailsViewState(movie),
        reducer = MovieDetailsViewStateReducer()
    )

    val viewState: LiveData<MovieDetailsViewState> = store.viewState

    private val viewEffectsStore = SingleEventStore<ViewEffect>()
    val viewEffects: LiveData<Event<ViewEffect>> = viewEffectsStore.events

    init {
        viewModelScope.launch {
            fetchMovieDetails()
            fetchWatchStatus()
            fetchSimilarMovies()
            fetchReviews()
        }
    }

    private suspend fun fetchMovieDetails() {
        val viewResult = try {
            val movie = checkNotNull(repository.fetchFullMovie(movie.id))
            val entity = viewEntitiesMapper(movie)
            ViewResult.MovieLoaded(entity)
        } catch (e: IOException) {
            ViewResult.None
        }
        store.dispatch(viewResult)
    }

    private suspend fun fetchSimilarMovies() {
        val viewResult = try {
            val movies = repository.fetchSimilarMovies(movie.id)
            val mapped = viewEntitiesMapper.mapPartialMovies(movies)
            ViewResult.RecommendationsLoaded(mapped)
        } catch (e: IOException) {
            ViewResult.None
        }
        store.dispatch(viewResult)
    }

    private suspend fun fetchReviews() {
        val viewResult = try {
            val reviews = repository.fetchReviews(movie.id)
            ViewResult.ReviewsLoaded(reviews)
        } catch (e: IOException) {
            ViewResult.None
        }
        store.dispatch(viewResult)
    }

    private suspend fun fetchWatchStatus() {
        val count = historyRepository.count(movie.id)
        val viewResult = if (count > 0) {
            ViewResult.LoadedWatchStatus(Movie.WatchStatus.WATCHED)
        } else {
            fetchWatchlistStatus()
        }
        store.dispatch(viewResult)
    }

    private suspend fun fetchWatchlistStatus(): ViewResult {
        val count = watchlistRepository.count(movie.id)
        return if (count > 0) {
            ViewResult.LoadedWatchStatus(Movie.WatchStatus.ON_WATCHLIST)
        } else {
            ViewResult.LoadedWatchStatus(Movie.WatchStatus.NOT_WATCHED)
        }
    }

    private fun loadTrailer() = viewModelScope.launch {
        val movie = checkNotNull(store.state().movie)
        store.dispatch(ViewResult.TrailerLoading)
        val url = repository.fetchVideo(movie.id, movie.title)

        store.dispatch(ViewResult.TrailerLoaded)
        viewEffectsStore.dispatch(ViewEffect.OpenTrailer(url))
    }

    private fun createImdbLink() {
        val movie = checkNotNull(store.state().movie as? MovieViewEntity.Full)
        val url = "http://www.imdb.com/title/${movie.raw.imdbId}"
        viewEffectsStore.dispatch(ViewEffect.OpenImdb(url))
    }

    private fun addToWatchlist() = viewModelScope.launch {
        val count = watchlistRepository.count(movie.id)
        if (count == 0) {
            val movie = checkNotNull(store.state().movie as? MovieViewEntity.Full)
            watchlistRepository.store(movie.raw)
        }
        store.dispatch(ViewResult.LoadedWatchStatus(Movie.WatchStatus.ON_WATCHLIST))
    }

    private fun removeFromWatchlist() = viewModelScope.launch {
        watchlistRepository.remove(movie.id)
        store.dispatch(ViewResult.LoadedWatchStatus(Movie.WatchStatus.NOT_WATCHED))
    }

    private fun loadColorPalette(bitmap: Bitmap) = viewModelScope.launch {
        val event = withContext(Dispatchers.IO) {
            try {
                val palette = Palette.from(bitmap).generate()
                ViewResult.ColorPaletteLoaded(palette)
            } catch (e: Exception) {
                ViewResult.None
            }
        }
        store.dispatch(event)
    }

    fun handleViewEvent(viewEvent: ViewEvent) {
        when (viewEvent) {
            is ViewEvent.AddToWatchlist -> addToWatchlist()
            is ViewEvent.LoadColorPalette -> loadColorPalette(viewEvent.bitmap)
            is ViewEvent.OpenImdb -> createImdbLink()
            is ViewEvent.OpenTrailer -> loadTrailer()
            is ViewEvent.RemoveFromWatchlist -> removeFromWatchlist()
        }
    }
}
