package com.hellmund.primetime.watchlist.ui

import com.hellmund.primetime.core.ValueFormatter
import com.hellmund.primetime.data.model.WatchlistMovie
import javax.inject.Inject
import org.threeten.bp.LocalDate.now

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
            "https://image.tmdb.org/t/p/w500${movie.posterUrl}",
            movie.description,
            movie.runtime > 0,
            valueFormatter.formatRuntime(movie.runtime),
            valueFormatter.formatReleaseYear(movie.releaseDate),
            movie.addedAt,
            movie.notificationsActivated,
            movie.releaseDate.isAfter(now()),
            movie
        )
    }
}
