package com.hellmund.primetime.recommendations.ui

import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.PartialMovie
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.recommendations.data.MoviesRepository
import org.threeten.bp.LocalDate

val FAKE_MOVIE = Movie(
    id = 1L,
    title = "1",
    posterPath = "poster",
    backdropPath = "backdrop",
    genres = listOf(
        Genre.Impl(id = 4L, name = "Genre 1", isPreferred = false, isExcluded = false),
        Genre.Impl(id = 5L, name = "Genre 2", isPreferred = false, isExcluded = false)
    ),
    description = "Lorem ipsum",
    releaseDate = LocalDate.now(),
    popularity = 7f,
    voteAverage = 7f,
    voteCount = 1_000,
    runtime = 123,
    imdbId = "imdb_id"
)

val FAKE_PARTIAL_MOVIES = listOf(
    PartialMovie(
        id = 1L,
        title = "1",
        posterPath = "poster",
        backdropPath = "backdrop",
        genreIds = listOf(1, 2, 3),
        description = "Lorem ipsum",
        releaseDate = LocalDate.now(),
        popularity = 7f,
        voteAverage = 7f,
        voteCount = 1_000
    ),
    PartialMovie(
        id = 2L,
        title = "2",
        posterPath = "poster",
        backdropPath = "backdrop",
        genreIds = listOf(1, 2, 3),
        description = "Lorem ipsum",
        releaseDate = LocalDate.now(),
        popularity = 7f,
        voteAverage = 7f,
        voteCount = 1_000
    ),
    PartialMovie(
        id = 3L,
        title = "3",
        posterPath = "poster",
        backdropPath = "backdrop",
        genreIds = listOf(1, 2, 3),
        description = "Lorem ipsum",
        releaseDate = LocalDate.now(),
        popularity = 7f,
        voteAverage = 7f,
        voteCount = 1_000
    )
)

class FakeMoviesRepository : MoviesRepository {

    override suspend fun fetchRecommendations(
        type: RecommendationsType,
        page: Int
    ): List<PartialMovie> = FAKE_PARTIAL_MOVIES

    override suspend fun fetchSimilarMovies(
        movieId: Long,
        page: Int
    ): List<PartialMovie> = FAKE_PARTIAL_MOVIES

    override suspend fun searchMovies(query: String): List<PartialMovie> = FAKE_PARTIAL_MOVIES

    override suspend fun fetchPopularMovies(): List<PartialMovie> = FAKE_PARTIAL_MOVIES

    override suspend fun fetchFullMovie(movieId: Long): Movie? = FAKE_MOVIE
}
