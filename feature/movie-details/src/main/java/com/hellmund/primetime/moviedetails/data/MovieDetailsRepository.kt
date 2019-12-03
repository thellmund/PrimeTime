package com.hellmund.primetime.moviedetails.data

import com.hellmund.api.TmdbApiService
import com.hellmund.api.model.Review
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.PartialMovie
import javax.inject.Inject

interface MovieDetailsRepository {
    suspend fun fetchFullMovie(movieId: Long): Movie?
    suspend fun fetchVideo(movieId: Long, title: String): String
    suspend fun fetchReviews(movieId: Long): List<Review>
    suspend fun fetchSimilarMovies(movieId: Long): List<PartialMovie>
}

class RealMovieDetailsRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val videoResolver: VideoResolver
) : MovieDetailsRepository {

    override suspend fun fetchFullMovie(movieId: Long): Movie? {
        val apiMovie = apiService.movie(movieId)
        return Movie.from(apiMovie)
    }

    override suspend fun fetchVideo(movieId: Long, title: String): String {
        val results = apiService.videos(movieId).results
        return videoResolver.findBest(title, results)
    }

    override suspend fun fetchReviews(
        movieId: Long
    ): List<Review> = apiService.reviews(movieId).results

    override suspend fun fetchSimilarMovies(
        movieId: Long
    ) = apiService.recommendations(movieId, page = 1).results.map { PartialMovie.from(it) }
}
