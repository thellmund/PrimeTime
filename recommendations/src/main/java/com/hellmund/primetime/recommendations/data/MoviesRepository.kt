package com.hellmund.primetime.recommendations.data

import com.hellmund.api.TmdbApiService
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.onboarding.OnboardingHelper
import javax.inject.Inject

interface MoviesRepository {
    suspend fun fetchRecommendations(type: RecommendationsType, page: Int): List<Movie>
    suspend fun fetchSimilarMovies(movieId: Int, page: Int = 1): List<Movie>
    suspend fun searchMovies(query: String): List<Movie>
    suspend fun fetchPopularMovies(): List<Movie>
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
    ): List<Movie> {
        return when (type) {
            is RecommendationsType.Personalized -> fetchPersonalizedRecommendations(type.genres, page)
            is RecommendationsType.BasedOnMovie -> fetchMovieBasedRecommendations(type.id, page)
            is RecommendationsType.NowPlaying -> fetchNowPlayingRecommendations(page)
            is RecommendationsType.Upcoming -> fetchUpcomingRecommendations(page)
            is RecommendationsType.ByGenre -> fetchGenreRecommendations(type.genre.id, page)
        }
    }

    private suspend fun fetchPersonalizedRecommendations(
        filterGenres: List<Genre>? = null,
        page: Int
    ): List<Movie> {
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
        movieId: Int,
        page: Int
    ) = fetchSimilarMovies(movieId, page)

    private suspend fun fetchNowPlayingRecommendations(
        page: Int
    ) = apiService.nowPlaying(page).results
        .filter { it.isValid }
        .map { Movie.from(it) }

    private suspend fun fetchUpcomingRecommendations(
        page: Int
    ): List<Movie> = apiService.upcoming(page).results
        .filter { it.isValid }
        .map { Movie.from(it) }

    override suspend fun fetchSimilarMovies(
        movieId: Int,
        page: Int
    ): List<Movie> = apiService.recommendations(movieId, page).results
        .filter { it.isValid }
        .map { Movie.from(it) }

    private suspend fun fetchGenreRecommendations(
        genreId: Int,
        page: Int = 1
    ) = apiService.genreRecommendations(genreId, page).results
        .filter { it.isValid }
        .map { Movie.from(it) }

    private suspend fun fetchTopRatedMovies(
        page: Int = 1
    ): List<Movie> = apiService.topRatedMovies(page).results
        .filter { it.isValid }
        .map { Movie.from(it) }

    override suspend fun searchMovies(
        query: String
    ): List<Movie> = apiService.search(query).results
        .filter { it.isValid }
        .map { Movie.from(it) }

    override suspend fun fetchPopularMovies(): List<Movie> {
        return apiService.popular().results.filter { it.isValid }.map { Movie.from(it) }
    }

}
