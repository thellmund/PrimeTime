package com.hellmund.primetime.ui.suggestions

import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.utils.ValueFormatter
import io.reactivex.functions.Function
import javax.inject.Inject

class MoviesViewEntityMapper @Inject constructor(
        valueFormatter: ValueFormatter
) : Function<List<Movie>, List<MovieViewEntity>> {

    private val internalMapper = MovieViewEntityMapper(valueFormatter)

    override fun apply(movies: List<Movie>): List<MovieViewEntity> {
        return movies.map(internalMapper::apply)
    }

}

class MovieViewEntityMapper @Inject constructor(
        private val valueFormatter: ValueFormatter
) : Function<Movie, MovieViewEntity> {

    override fun apply(movie: Movie): MovieViewEntity {
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
