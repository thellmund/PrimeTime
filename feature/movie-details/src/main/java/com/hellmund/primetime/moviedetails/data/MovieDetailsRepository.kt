package com.hellmund.primetime.moviedetails.data

import com.hellmund.api.TmdbApiService
import com.hellmund.api.model.Review
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.MovieEnricher
import javax.inject.Inject

interface MovieDetailsRepository {
    suspend fun fetchVideo(movieId: Long, title: String): String
    suspend fun fetchReviews(movieId: Long): List<Review>
    suspend fun fetchSimilarMovies(movieId: Long): List<Movie>
}

class RealMovieDetailsRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val enricher: MovieEnricher,
    private val videoResolver: VideoResolver
) : MovieDetailsRepository {

    override suspend fun fetchVideo(movieId: Long, title: String): String {
        val results = apiService.videos(movieId).results
        return videoResolver.findBest(title, results)
    }

    override suspend fun fetchReviews(
        movieId: Long
    ): List<Review> = apiService.reviews(movieId).results

    override suspend fun fetchSimilarMovies(
        movieId: Long
    ): List<Movie> = apiService.recommendations(movieId, page = 1)
        .results
        .mapNotNull { enricher.enrich(it) }
}
