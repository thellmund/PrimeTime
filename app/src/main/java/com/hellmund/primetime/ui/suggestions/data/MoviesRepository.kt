package com.hellmund.primetime.ui.suggestions.data

import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.VideoResolver
import com.hellmund.primetime.ui.suggestions.details.Review
import com.hellmund.primetime.utils.OnboardingHelper
import javax.inject.Inject

data class MoviesResponse(val results: List<Movie>)

interface MoviesRepository {
    suspend fun fetchRecommendations(type: RecommendationsType, page: Int): List<Movie>
    suspend fun fetchRecommendations(movieId: Int, page: Int = 1): List<Movie>
    suspend fun fetchVideo(movie: MovieViewEntity): String
    suspend fun fetchMovie(movieId: Int): Movie
    suspend fun searchMovies(query: String): List<Movie>
    suspend fun fetchPopularMovies(): List<Movie>
    suspend fun fetchReviews(movieId: Int): List<Review>
}

class RealMoviesRepository @Inject constructor(
        private val apiService: ApiService,
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
        // TODO: Fix this workaround
        // TODO: Add new movies to results
        if (onboardingHelper.isFirstLaunch) {
            return fetchTopRatedMovies(page)
        }

        val history = historyRepository.getLiked()
            .sorted()
            .take(10)

        val personalized = history.map { fetchRecommendations(it.id, page) }.flatten()
        val genres = filterGenres ?: genresRepository.getPreferredGenres()
        val byGenre = genres.map { fetchGenreRecommendations(it.id, page) }.flatten()
        val topRated = fetchTopRatedMovies(page)

        return personalized + byGenre + topRated
    }

    private suspend fun fetchMovieBasedRecommendations(
        movieId: Int,
        page: Int
    ) = fetchRecommendations(movieId, page)

    private suspend fun fetchNowPlayingRecommendations(
            page: Int
    ) = apiService.nowPlaying(page).results

    private suspend fun fetchUpcomingRecommendations(
            page: Int
    ): List<Movie> {
        return apiService.upcoming(page).results
    }

    override suspend fun fetchRecommendations(
            movieId: Int,
            page: Int
    ): List<Movie> {
        return apiService.recommendations(movieId, page).results
    }

    private suspend fun fetchGenreRecommendations(
            genreId: Int,
            page: Int = 1
    ): List<Movie> {
        return apiService.genreRecommendations(genreId, page).results
    }

    private suspend fun fetchTopRatedMovies(
            page: Int = 1
    ): List<Movie> {
        return apiService.topRatedMovies(page).results
    }

    override suspend fun fetchVideo(movie: MovieViewEntity): String {
        val results = apiService.videos(movie.id).results
        return VideoResolver.findBest(movie.title, results)
    }

    override suspend fun fetchMovie(movieId: Int) = apiService.movie(movieId)

    override suspend fun searchMovies(query: String): List<Movie> {
        return apiService.search(query).results
    }

    override suspend fun fetchPopularMovies(): List<Movie> {
        return apiService.popular().results
    }

    override suspend fun fetchReviews(movieId: Int): List<Review> {
        return apiService.reviews(movieId).results
    }

}
