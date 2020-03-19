package com.hellmund.primetime.recommendations.data

import com.hellmund.primetime.data.model.PartialMovie
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.data.repositories.WatchlistRepository
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.threeten.bp.LocalDate

data class MovieWithScore(
    val movie: PartialMovie,
    val score: Float
)

class MovieRankingProcessor @Inject constructor(
    private val historyRepo: HistoryRepository,
    private val watchlistRepo: WatchlistRepository
) {

    suspend operator fun invoke(
        movies: List<PartialMovie>,
        type: RecommendationsType
    ): List<PartialMovie> {
        val knownMovies = coroutineScope {
            val watchedMovies = historyRepo.observeAll().first().map { it.id }.toSet()
            val watchlist = watchlistRepo.observeAll().first().map { it.id }.toSet()
            watchedMovies + watchlist
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

    private fun isReleased(
        movie: PartialMovie,
        type: RecommendationsType
    ): Boolean = when (type) {
        RecommendationsType.Upcoming -> true
        else -> movie.releaseDate?.isBefore(LocalDate.now()) ?: false
    }

    private fun hasEnoughInformation(movie: PartialMovie): Boolean {
        val hasGenres = movie.genreIds.isNotEmpty()
        val hasDescription = movie.description != null
        return hasGenres && hasDescription && movie.voteAverage > 0f
    }

    private fun adjustRating(movie: PartialMovie): MovieWithScore {
        // TODO Implement this
        return MovieWithScore(movie, movie.voteAverage)
    }
}
