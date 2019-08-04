package com.hellmund.primetime.ui.watchlist

import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.utils.ValueFormatter
import com.hellmund.primetime.utils.isAfterNow
import javax.inject.Inject

class WatchlistMovieViewEntityMapper @Inject constructor(
        private val valueFormatter: ValueFormatter
) {

    operator fun invoke(
        movies: List<WatchlistMovie>
    ) = movies.map(this::convert)

    private fun convert(movie: WatchlistMovie): WatchlistMovieViewEntity {
        return WatchlistMovieViewEntity(
            movie.id,
            movie.title,
            "https://image.tmdb.org/t/p/w500${movie.posterURL}",
            movie.description,
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
