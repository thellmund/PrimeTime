package com.hellmund.primetime.main

import com.hellmund.primetime.database.HistoryMovie
import com.hellmund.primetime.database.WatchlistMovie
import com.hellmund.primetime.history.HistoryRepository
import com.hellmund.primetime.model2.ApiMovie
import com.hellmund.primetime.watchlist.WatchlistRepository
import java.util.*

data class MovieWithScore(
        val movie: ApiMovie,
        val score: Float
)

class MovieRankingProcessor(
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
            movies: List<ApiMovie>, type: RecommendationsType
    ): List<ApiMovie> {
        return movies
                .asSequence()
                .filter { isKnownMovie(it) }
                .filter { isReleased(it, type) }
                .map { adjustRating(it) }
                .sortedBy { it.score }
                .map { it.movie }
                .toList()
    }

    private fun isKnownMovie(apiMovie: ApiMovie): Boolean {
        val hasSeenMovie = watchedMovies.contains(apiMovie.id)
        val isOnWatchlist = moviesOnWatchlist.contains(apiMovie.id)
        return hasSeenMovie.not() && isOnWatchlist.not()
    }


    private fun isReleased(apiMovie: ApiMovie, type: RecommendationsType): Boolean {
        return when (type) {
            RecommendationsType.Upcoming -> true
            else -> apiMovie.releaseDate?.before(Date()) ?: false
        }
    }

    private fun adjustRating(apiMovie: ApiMovie): MovieWithScore {
        // TODO
        return MovieWithScore(apiMovie, apiMovie.voteAverage)
    }

}
