package com.hellmund.primetime.recommendations.data

import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.data.repositories.WatchlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    suspend operator fun invoke(
        movies: List<Movie>,
        type: RecommendationsType
    ): List<Movie> {
        val knownMovies = coroutineScope {
            val watchedMovies = async { historyRepo.getAll().map { it.id }.toSet() }
            val watchlist = async { watchlistRepo.getAll().map { it.id }.toSet() }
            watchedMovies.await() + watchlist.await()
        }

        return movies
            .asSequence()
            .distinct()
            .filter { knownMovies.contains(it.id).not() }
            .filter { isReleased(it, type) }
            .filter { hasEnoughInformation(it) }
            .map { adjustRating(it) }
            .sortedBy { it.score }
            .map { it.movie }
            .toList()
    }

    private fun isReleased(movie: Movie, type: RecommendationsType): Boolean {
        return when (type) {
            RecommendationsType.Upcoming -> true
            else -> movie.releaseDate?.isBefore(LocalDate.now()) ?: false
        }
    }

    private fun hasEnoughInformation(movie: Movie): Boolean {
        val hasGenres = movie.genres.isNotEmpty()
        return hasGenres && movie.description.isNotEmpty() && movie.voteAverage > 0f
    }

    private fun adjustRating(movie: Movie): MovieWithScore {
        // TODO Implement this
        return MovieWithScore(movie, movie.voteAverage)
    }
}
