package com.hellmund.primetime.recommendations.data

import com.hellmund.api.TmdbApiService
import com.hellmund.primetime.core.OnboardingHelper
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.PartialMovie
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.repositories.HistoryRepository
import javax.inject.Inject

interface MoviesRepository {
    suspend fun fetchRecommendations(type: RecommendationsType, page: Int): List<PartialMovie>
    suspend fun fetchSimilarMovies(movieId: Long, page: Int = 1): List<PartialMovie>
    suspend fun searchMovies(query: String): List<PartialMovie>
    suspend fun fetchPopularMovies(): List<PartialMovie>
    suspend fun fetchFullMovie(movieId: Long): Movie?
}

class RealMoviesRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val genresRepository: GenresRepository,
    private val historyRepository: HistoryRepository,
    private val onboardingHelper: OnboardingHelper
) : MoviesRepository {

    override suspend fun fetchRecommendations(
        type: RecommendationsType,
        page: Int
    ): List<PartialMovie> = when (type) {
        is RecommendationsType.Personalized -> fetchPersonalizedRecommendations(type.genres, page)
        is RecommendationsType.BasedOnMovie -> fetchMovieBasedRecommendations(type.id, page)
        is RecommendationsType.NowPlaying -> fetchNowPlayingRecommendations(page)
        is RecommendationsType.Upcoming -> fetchUpcomingRecommendations(page)
        is RecommendationsType.ByGenre -> fetchGenreRecommendations(type.genre.id, page)
    }

    private suspend fun fetchPersonalizedRecommendations(
        filterGenres: List<Genre>? = null,
        page: Int
    ): List<PartialMovie> {
        // TODO: Add new movies to results
        if (onboardingHelper.isFirstLaunch) {
            return fetchTopRatedMovies(page)
        }

        val history = historyRepository.getLiked()
            .sortedByDescending { it.timestamp }
            .take(10)

        val personalized = history.map { fetchSimilarMovies(it.id, page) }.flatten()
        val genres = filterGenres ?: genresRepository.getPreferredGenres()
        val byGenre = genres.map { fetchGenreRecommendations(it.id, page) }.flatten()
        val topRated = fetchTopRatedMovies(page)

        return personalized + byGenre + topRated
    }

    private suspend fun fetchMovieBasedRecommendations(
        movieId: Long,
        page: Int
    ) = fetchSimilarMovies(movieId, page)

    private suspend fun fetchNowPlayingRecommendations(
        page: Int
    ) = apiService.nowPlaying(page).results.map { PartialMovie.from(it) }

    private suspend fun fetchUpcomingRecommendations(
        page: Int
    ) = apiService.upcoming(page).results.map { PartialMovie.from(it) }

    override suspend fun fetchFullMovie(movieId: Long): Movie? {
        val apiMovie = apiService.movie(movieId)
        return Movie.from(apiMovie)
    }

    override suspend fun fetchSimilarMovies(
        movieId: Long,
        page: Int
    ) = apiService.recommendations(movieId, page).results.map { PartialMovie.from(it) }

    private suspend fun fetchGenreRecommendations(
        genreId: Long,
        page: Int = 1
    ) = apiService.genreRecommendations(genreId, page).results.map { PartialMovie.from(it) }

    private suspend fun fetchTopRatedMovies(
        page: Int = 1
    ) = apiService.topRatedMovies(page).results.map { PartialMovie.from(it) }

    override suspend fun searchMovies(
        query: String
    ) = apiService.search(query).results.map { PartialMovie.from(it) }

    override suspend fun fetchPopularMovies(): List<PartialMovie> {
        return apiService.popular().results.map { PartialMovie.from(it) }
    }
}
