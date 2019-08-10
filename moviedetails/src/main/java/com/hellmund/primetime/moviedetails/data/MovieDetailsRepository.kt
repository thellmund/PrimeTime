package com.hellmund.primetime.moviedetails.data

import com.hellmund.api.Review
import com.hellmund.api.TmdbApiService
import com.hellmund.primetime.data.model.Movie
import javax.inject.Inject

interface MovieDetailsRepository {
    suspend fun fetchMovie(movieId: Int): Movie
    suspend fun fetchVideo(movieId: Int, title: String): String
    suspend fun fetchReviews(movieId: Int): List<Review>
    suspend fun fetchSimilarMovies(movieId: Int): List<Movie>
}

class RealMovieDetailsRepository @Inject constructor(
    private val apiService: TmdbApiService
) : MovieDetailsRepository {

    override suspend fun fetchVideo(movieId: Int, title: String): String {
        val results = apiService.videos(movieId).results
        return VideoResolver.findBest(title, results)
    }

    override suspend fun fetchMovie(
        movieId: Int
    ): Movie = Movie.from(apiService.movie(movieId))

    override suspend fun fetchReviews(
        movieId: Int
    ): List<Review> = apiService.reviews(movieId).results

    override suspend fun fetchSimilarMovies(
        movieId: Int
    ): List<Movie> = apiService.recommendations(movieId, page = 1).results
        .filter { it.isValid }
        .map { Movie.from(it) }

}
