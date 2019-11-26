package com.hellmund.primetime.ui_common

import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.core.ValueFormatter
import javax.inject.Inject

class MovieViewEntitiesMapper @Inject constructor(
    private val valueFormatter: ValueFormatter
) {

    operator fun invoke(
        movies: List<Movie>
    ): List<MovieViewEntity> = movies.map { invoke(it) }

    operator fun invoke(movie: Movie): MovieViewEntity {
        return MovieViewEntity(
            id = movie.id,
            posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
            backdropUrl = "https://image.tmdb.org/t/p/w500${movie.backdropPath}",
            title = movie.title,
            formattedGenres = valueFormatter.formatGenres(movie.genres),
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

// class MovieViewEntityMapper @Inject constructor(
//    private val valueFormatter: ValueFormatter
// ) {
//
//    operator fun invoke(movie: FullMovie): MovieViewEntity {
//        return MovieViewEntity(
//            id = movie.id,
//            posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
//            backdropUrl = "https://image.tmdb.org/t/p/w500${movie.backdropPath}",
//            title = movie.title,
//            formattedGenres = valueFormatter.formatGenres(movie.genres),
//            description = movie.description,
//            releaseYear = valueFormatter.formatReleaseYear(movie.releaseDate),
//            popularity = movie.popularity,
//            formattedVoteAverage = "${movie.voteAverage} / 10",
//            formattedVoteCount = valueFormatter.formatCount(movie.voteCount),
//            formattedRuntime = valueFormatter.formatRuntime(movie.runtime),
//            imdbId = movie.imdbId,
//            raw = movie
//        )
//    }
//
// }
