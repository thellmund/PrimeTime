package com.hellmund.primetime.ui_common

import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.ui_common.util.ValueFormatter
import javax.inject.Inject

class MovieViewEntitiesMapper @Inject constructor(
    valueFormatter: ValueFormatter,
    genresRepository: GenresRepository
) {

    private val internalMapper = MovieViewEntityMapper(valueFormatter, genresRepository)

    suspend operator fun invoke(movies: List<Movie>): List<MovieViewEntity> {
        return movies.map { internalMapper(it) }
    }

}

class MovieViewEntityMapper @Inject constructor(
    private val valueFormatter: ValueFormatter,
    private val genresRepository: GenresRepository
) {

    suspend operator fun invoke(movie: Movie): MovieViewEntity {
        val genres = if (movie.genres.isNullOrEmpty()) {
            fetchGenresIfNecessary()
            movie.genreIds.orEmpty().map { genresRepository.getGenreById(it) }
        } else {
            movie.genres.orEmpty()
        }

        return MovieViewEntity(
            id = movie.id,
            posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
            backdropUrl = "https://image.tmdb.org/t/p/w500${movie.backdropPath}",
            title = movie.title,
            formattedGenres = valueFormatter.formatGenres(genres),
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

    private suspend fun fetchGenresIfNecessary() {
        val size = genresRepository.getAll().size
        if (genresRepository.getAll().isEmpty()) {
            val genres = genresRepository.fetchGenres()
            genresRepository.storeGenres(genres)
        }
    }

}