package com.hellmund.primetime.search.ui

import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui_common.util.ValueFormatter
import javax.inject.Inject

class SearchViewEntitiesMapper @Inject constructor(
    valueFormatter: ValueFormatter,
    genresRepository: GenresRepository
) {

    private val internalMapper = SearchViewEntityMapper(valueFormatter, genresRepository)

    suspend operator fun invoke(movies: List<Movie>): List<SearchViewEntity> {
        return movies.map { internalMapper(it) }
    }

}

class SearchViewEntityMapper @Inject constructor(
    private val valueFormatter: ValueFormatter,
    private val genresRepository: GenresRepository
) {

    suspend operator fun invoke(movie: Movie): SearchViewEntity {
        val genres = if (movie.genres.isNullOrEmpty()) {
            movie.genreIds.orEmpty().map { genresRepository.getGenre(it.toString()) }
        } else {
            movie.genres.orEmpty()
        }

        return SearchViewEntity(
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

}
