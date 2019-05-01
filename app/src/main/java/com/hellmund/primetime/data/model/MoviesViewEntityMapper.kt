package com.hellmund.primetime.data.model

import com.hellmund.primetime.utils.ValueFormatter
import io.reactivex.functions.Function
import javax.inject.Inject

class MoviesViewEntityMapper @Inject constructor(
        private val valueFormatter: ValueFormatter
) : Function<List<Movie>, List<MovieViewEntity>> {

    override fun apply(movies: List<Movie>): List<MovieViewEntity> {
        return movies.map(this::createViewEntity)
    }

    private fun createViewEntity(movie: Movie): MovieViewEntity {
        return MovieViewEntity(
                id = movie.id,
                posterUrl = "http://image.tmdb.org/t/p/w500${movie.posterPath}",
                title = movie.title,
                formattedGenres = valueFormatter.formatGenres(movie),
                description = movie.description,
                releaseYear = valueFormatter.formatReleaseYear(movie.releaseDate),
                popularity = movie.popularity,
                formattedVoteAverage = "${movie.voteAverage} / 10",
                formattedRuntime = valueFormatter.formatRuntime(movie.runtime),
                imdbId = movie.imdbId,
                raw = movie
        )
    }

}

class MovieViewEntityMapper @Inject constructor(
        private val valueFormatter: ValueFormatter
) : Function<Movie, MovieViewEntity> {

    override fun apply(movie: Movie): MovieViewEntity {
        return MovieViewEntity(
                id = movie.id,
                posterUrl = "http://image.tmdb.org/t/p/w500${movie.posterPath}",
                title = movie.title,
                formattedGenres = valueFormatter.formatGenres(movie),
                description = movie.description,
                releaseYear = valueFormatter.formatReleaseYear(movie.releaseDate),
                popularity = movie.popularity,
                formattedVoteAverage = "${movie.voteAverage} / 10",
                formattedRuntime = valueFormatter.formatRuntime(movie.runtime),
                imdbId = movie.imdbId,
                raw = movie
        )
    }

}
