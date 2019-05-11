package com.hellmund.primetime.ui.suggestions.data

import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.watchlist.WatchlistRepository
import org.threeten.bp.LocalDate
import javax.inject.Inject

data class MovieWithScore(
        val movie: Movie,
        val score: Float
)

class MovieRankingProcessor @Inject constructor(
        private val historyRepo: HistoryRepository,
        private val watchlistRepo: WatchlistRepository
) {

    private val history: List<HistoryMovie>
        get() = historyRepo.getAll().blockingGet()

    private val watchlist: List<WatchlistMovie>
        get() = watchlistRepo.getAll().blockingGet()

    private val watchedMovies: Set<Int>
        get() = history.map { it.id }.toSet()

    private val moviesOnWatchlist: Set<Int>
        get() = watchlist.map { it.id }.toSet()

    fun rank(
            movies: List<Movie>, type: RecommendationsType
    ): List<Movie> {
        return movies
                .asSequence()
                .distinct()
                .filter { isKnownMovie(it) }
                .filter { isReleased(it, type) }
                .filter { hasEnoughInformation(it) }
                .map { adjustRating(it) }
                .sortedBy { it.score }
                .map { it.movie }
                .toList()
    }

    private fun isKnownMovie(movie: Movie): Boolean {
        val hasSeenMovie = watchedMovies.contains(movie.id)
        val isOnWatchlist = moviesOnWatchlist.contains(movie.id)
        return hasSeenMovie.not() && isOnWatchlist.not()
    }

    private fun isReleased(movie: Movie, type: RecommendationsType): Boolean {
        return when (type) {
            RecommendationsType.Upcoming -> true
            else -> movie.releaseDate?.isBefore(LocalDate.now()) ?: false
        }
    }

    private fun hasEnoughInformation(movie: Movie): Boolean {
        val hasNoGenres = movie.genreIds.isNullOrEmpty() && movie.genres.isNullOrEmpty()
        return hasNoGenres.not() && movie.description.isNotEmpty() && movie.voteAverage > 0f
    }

    private fun adjustRating(movie: Movie): MovieWithScore {
        // TODO
        return MovieWithScore(movie, movie.voteAverage)
    }

}
