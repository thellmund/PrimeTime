package com.hellmund.primetime.moviedetails.data

import com.hellmund.api.TmdbApiService
import com.hellmund.api.model.ApiResult
import com.hellmund.api.model.Review
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.PartialMovie
import java.io.IOException
import javax.inject.Inject

interface MovieDetailsRepository {
    suspend fun fetchFullMovie(movieId: Long): ApiResult<Movie>
    suspend fun fetchVideo(movieId: Long, title: String): ApiResult<String>
    suspend fun fetchReviews(movieId: Long): ApiResult<List<Review>>
    suspend fun fetchSimilarMovies(movieId: Long): ApiResult<List<PartialMovie>>
}

class RealMovieDetailsRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val videoResolver: VideoResolver
) : MovieDetailsRepository {

    override suspend fun fetchFullMovie(movieId: Long): ApiResult<Movie> {
        return try {
            val apiMovie = apiService.movie(movieId)
            val movie = Movie.from(apiMovie)

            if (movie != null) {
                ApiResult.Success(movie)
            } else {
                ApiResult.Failure()
            }
        } catch (e: IOException) {
            ApiResult.Failure(e)
        }
    }

    override suspend fun fetchVideo(movieId: Long, title: String): ApiResult<String> {
        return try {
            val results = apiService.videos(movieId).results
            val result = videoResolver.findBest(title, results)
            ApiResult.Success(result)
        } catch (e: IOException) {
            ApiResult.Failure(e)
        }
    }

    override suspend fun fetchReviews(
        movieId: Long
    ): ApiResult<List<Review>> {
        return try {
            val reviews = apiService.reviews(movieId).results
            ApiResult.Success(reviews)
        } catch (e: IOException) {
            ApiResult.Failure(e)
        }
    }

    override suspend fun fetchSimilarMovies(
        movieId: Long
    ): ApiResult<List<PartialMovie>> {
        return try {
            val movies = apiService.recommendations(movieId, page = 1).results.map { PartialMovie.from(it) }
            ApiResult.Success(movies)
        } catch (e: IOException) {
            ApiResult.Failure(e)
        }
    }
}
