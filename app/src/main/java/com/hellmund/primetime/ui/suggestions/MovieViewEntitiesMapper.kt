package com.hellmund.primetime.ui.suggestions

import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.utils.ValueFormatter
import javax.inject.Inject

class MovieViewEntitiesMapper @Inject constructor(
    valueFormatter: ValueFormatter
) {

    private val internalMapper = MovieViewEntityMapper(valueFormatter)

    suspend operator fun invoke(movies: List<Movie>) = movies.map { internalMapper(it) }

}

class MovieViewEntityMapper @Inject constructor(
    private val valueFormatter: ValueFormatter
) {

    suspend operator fun invoke(movie: Movie): MovieViewEntity {
        return MovieViewEntity(
            id = movie.id,
            posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
            backdropUrl = "https://image.tmdb.org/t/p/w500${movie.backdropPath}",
            title = movie.title,
            formattedGenres = valueFormatter.formatGenres(movie),
            description = movie.description,
            releaseYear = valueFormatter.formatReleaseYear(movie.releaseDate),
            popularity = movie.popularity,
            formattedVoteAverage = "${movie.voteAverage} / 10",
            formattedVoteCount = valueFormatter.formatCount(movie.voteCount),
            formattedRuntime = valueFormatter.formatRuntime(movie.runtime),
            imdbId = movie.imdbId,
            raw = movie
        )
    }

}
