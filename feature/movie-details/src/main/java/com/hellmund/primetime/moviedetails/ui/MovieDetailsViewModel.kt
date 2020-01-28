package com.hellmund.primetime.moviedetails.ui

import android.graphics.Bitmap
import android.util.Log
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
import com.hellmund.primetime.ui_common.PartialMovieViewEntity
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.SingleEvent
import com.hellmund.primetime.ui_common.viewmodel.SingleEventStore
import com.hellmund.primetime.ui_common.viewmodel.ViewStateStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

sealed class ViewEvent {
    object AddToWatchlist : ViewEvent()
    data class LoadColorPalette(val bitmap: Bitmap) : ViewEvent()
    object OpenImdb : ViewEvent()
    object OpenTrailer : ViewEvent()
    data class RecommendationClicked(val movieId: Long) : ViewEvent()
    object RemoveFromWatchlist : ViewEvent()
}

sealed class ViewResult {
    data class RecommendationsLoaded(val movies: List<PartialMovieViewEntity>) : ViewResult()
    data class ReviewsLoaded(val reviews: List<Review>) : ViewResult()
    object TrailerLoading : ViewResult()
    object TrailerLoaded : ViewResult()
    data class LoadedWatchStatus(val watchStatus: Movie.WatchStatus) : ViewResult()
    data class ColorPaletteLoaded(val palette: Palette) : ViewResult()
    object None : ViewResult()
}

sealed class NavigationResult {
    data class OpenTrailer(val url: String) : NavigationResult()
    data class OpenImdb(val url: String) : NavigationResult()
    data class OpenSimilarMovie(val viewEntity: MovieViewEntity) : NavigationResult()
}

data class MovieDetailsViewState(
    val movie: MovieViewEntity,
    val recommendations: List<PartialMovieViewEntity>? = null,
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
        is ViewResult.RecommendationsLoaded -> state.copy(recommendations = viewResult.movies)
        is ViewResult.TrailerLoading -> state.copy(isTrailerLoading = true)
        is ViewResult.TrailerLoaded -> state.copy(isTrailerLoading = false)
        is ViewResult.ReviewsLoaded -> state.copy(reviews = viewResult.reviews)
        is ViewResult.LoadedWatchStatus -> state.copy(watchStatus = viewResult.watchStatus)
        is ViewResult.ColorPaletteLoaded -> state.copy(color = viewResult.palette.mutedSwatch?.rgb)
        is ViewResult.None -> state
    }.also {
        Log.d("ViewStateReducer", "Reacted to ${viewResult.javaClass.simpleName}")
    }
}

class MovieDetailsViewStateStore(
    movie: MovieViewEntity
) : ViewStateStore<MovieDetailsViewState, ViewResult>(
    initialState = MovieDetailsViewState(movie),
    reducer = MovieDetailsViewStateReducer()
)

class MovieDetailsViewModel @Inject constructor(
    private val repository: MovieDetailsRepository,
    private val historyRepository: HistoryRepository,
    private val watchlistRepository: WatchlistRepository,
    private val viewEntitiesMapper: MovieViewEntitiesMapper,
    private var movie: MovieViewEntity
) : ViewModel() {

    private val viewStateStore = MovieDetailsViewStateStore(movie)
    val viewState: LiveData<MovieDetailsViewState> = viewStateStore.viewState

    private val navigationResultStore = SingleEventStore<NavigationResult>()
    val navigationResults: LiveData<SingleEvent<NavigationResult>> = navigationResultStore.events

    init {
        viewModelScope.launch {
            viewStateStore.dispatch(fetchWatchStatus())
            viewStateStore.dispatch(fetchSimilarMovies())
            viewStateStore.dispatch(fetchReviews())
        }
    }

    private suspend fun fetchSimilarMovies(): ViewResult {
        return try {
            val movies = repository.fetchSimilarMovies(movie.id)
            val mapped = viewEntitiesMapper.mapPartialMovies(movies)
            ViewResult.RecommendationsLoaded(mapped)
        } catch (e: IOException) {
            ViewResult.None
        }
    }

    private suspend fun fetchReviews(): ViewResult {
        return try {
            val reviews = repository.fetchReviews(movie.id)
            ViewResult.ReviewsLoaded(reviews)
        } catch (e: IOException) {
            ViewResult.None
        }
    }

    private suspend fun fetchWatchStatus(): ViewResult {
        val count = historyRepository.count(movie.id)
        return if (count > 0) {
            ViewResult.LoadedWatchStatus(Movie.WatchStatus.WATCHED)
        } else {
            fetchWatchlistStatus()
        }
    }

    private suspend fun fetchWatchlistStatus(): ViewResult {
        val count = watchlistRepository.count(movie.id)
        return if (count > 0) {
            ViewResult.LoadedWatchStatus(Movie.WatchStatus.ON_WATCHLIST)
        } else {
            ViewResult.LoadedWatchStatus(Movie.WatchStatus.NOT_WATCHED)
        }
    }

    private suspend fun loadTrailer() {
        viewStateStore.dispatch(ViewResult.TrailerLoading)
        val url = repository.fetchVideo(movie.id, movie.title)
        viewStateStore.dispatch(ViewResult.TrailerLoaded)
        navigationResultStore.dispatch(NavigationResult.OpenTrailer(url))
    }

    private fun createImdbLink() {
        val url = "http://www.imdb.com/title/${movie.raw.imdbId}"
        navigationResultStore.dispatch(NavigationResult.OpenImdb(url))
    }

    private suspend fun loadFullMovie(movieId: Long) {
        val movie = checkNotNull(repository.fetchFullMovie(movieId))
        val viewEntity = viewEntitiesMapper(movie)
        navigationResultStore.dispatch(NavigationResult.OpenSimilarMovie(viewEntity))
    }

    private suspend fun addToWatchlist() {
        val count = watchlistRepository.count(movie.id)
        if (count == 0) {
            watchlistRepository.store(movie.raw)
        }
        viewStateStore.dispatch(ViewResult.LoadedWatchStatus(Movie.WatchStatus.ON_WATCHLIST))
    }

    private suspend fun removeFromWatchlist() {
        watchlistRepository.remove(movie.id)
        viewStateStore.dispatch(ViewResult.LoadedWatchStatus(Movie.WatchStatus.NOT_WATCHED))
    }

    private suspend fun loadColorPalette(bitmap: Bitmap) {
        val event = withContext(Dispatchers.IO) {
            try {
                val palette = Palette.from(bitmap).generate()
                ViewResult.ColorPaletteLoaded(palette)
            } catch (e: Exception) {
                ViewResult.None
            }
        }
        viewStateStore.dispatch(event)
    }

    fun dispatch(viewEvent: ViewEvent) {
        viewModelScope.launch {
            when (viewEvent) {
                is ViewEvent.AddToWatchlist -> addToWatchlist()
                is ViewEvent.LoadColorPalette -> loadColorPalette(viewEvent.bitmap)
                is ViewEvent.OpenImdb -> createImdbLink()
                is ViewEvent.OpenTrailer -> loadTrailer()
                is ViewEvent.RecommendationClicked -> loadFullMovie(viewEvent.movieId)
                is ViewEvent.RemoveFromWatchlist -> removeFromWatchlist()
            }
        }
    }
}
