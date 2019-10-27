package com.hellmund.primetime.moviedetails.data

import com.hellmund.api.TmdbApiService
import com.hellmund.api.model.Review
import com.hellmund.primetime.data.model.Movie
import javax.inject.Inject

interface MovieDetailsRepository {
    suspend fun fetchMovie(movieId: Long): Movie
    suspend fun fetchVideo(movieId: Long, title: String): String
    suspend fun fetchReviews(movieId: Long): List<Review>
    suspend fun fetchSimilarMovies(movieId: Long): List<Movie>
}

class RealMovieDetailsRepository @Inject constructor(
    private val apiService: TmdbApiService
) : MovieDetailsRepository {

    override suspend fun fetchVideo(movieId: Long, title: String): String {
        val results = apiService.videos(movieId).results
        return VideoResolver.findBest(title, results)
    }

    override suspend fun fetchMovie(
        movieId: Long
    ): Movie = Movie.from(apiService.movie(movieId))

    override suspend fun fetchReviews(
        movieId: Long
    ): List<Review> = apiService.reviews(movieId).results

    override suspend fun fetchSimilarMovies(
        movieId: Long
    ): List<Movie> = apiService.recommendations(movieId, page = 1).results
        .filter { it.isValid }
        .map { Movie.from(it) }

}
