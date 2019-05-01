package com.hellmund.primetime.ui.watchlist

import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.utils.ValueFormatter
import com.hellmund.primetime.utils.isAfterNow
import io.reactivex.functions.Function
import javax.inject.Inject

class WatchlistMovieViewEntityMapper @Inject constructor(
        private val valueFormatter: ValueFormatter
) : Function<List<WatchlistMovie>, List<WatchlistMovieViewEntity>> {

    override fun apply(movies: List<WatchlistMovie>): List<WatchlistMovieViewEntity> {
        return movies.map(this::convert)
    }

    private fun convert(movie: WatchlistMovie): WatchlistMovieViewEntity {
        return WatchlistMovieViewEntity(
                movie.id,
                movie.title,
                "https://image.tmdb.org/t/p/w500${movie.posterURL}",
                movie.runtime > 0,
                valueFormatter.formatRuntime(movie.runtime),
                valueFormatter.formatReleaseYear(movie.releaseDate),
                movie.timestamp,
                movie.notificationsActivated,
                movie.releaseDate.isAfterNow,
                movie
        )
    }

}
