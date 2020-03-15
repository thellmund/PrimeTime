package com.hellmund.primetime.ui_common

import com.hellmund.primetime.core.ValueFormatter
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.MovieGenre
import com.hellmund.primetime.data.model.PartialMovie
import com.hellmund.primetime.data.repositories.GenresRepository
import javax.inject.Inject

class MovieViewEntitiesMapper @Inject constructor(
    private val valueFormatter: ValueFormatter,
    private val genresRepository: GenresRepository
) {

    suspend fun mapPartialMovies(
        movies: List<PartialMovie>
    ): List<PartialMovieViewEntity> = movies.map { mapPartialMovie(it) }

    private suspend fun mapPartialMovie(movie: PartialMovie): PartialMovieViewEntity {
        val genres = movie.genreIds
            .map { genresRepository.getGenreById(it) }
            .map { it.toMovieGenre() }

        return PartialMovieViewEntity(
            id = movie.id,
            posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
            backdropUrl = "https://image.tmdb.org/t/p/w500${movie.backdropPath}",
            title = movie.title,
            formattedGenres = valueFormatter.formatGenres(genres),
            description = valueFormatter.formatDescription(movie.description),
            releaseYear = valueFormatter.formatReleaseYear(movie.releaseDate),
            popularity = movie.popularity,
            formattedVoteAverage = "${movie.voteAverage} / 10",
            formattedVoteCount = valueFormatter.formatCount(movie.voteCount),
            formattedRuntime = valueFormatter.formatRuntime(null),
            imdbId = null,
            raw = movie
        )
    }

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

private fun Genre.toMovieGenre(): MovieGenre = MovieGenre(id, name, isPreferred, isExcluded)
