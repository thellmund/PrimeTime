package com.hellmund.primetime.search.data

import com.hellmund.api.TmdbApiService
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.PartialMovie
import javax.inject.Inject

interface SearchRepository {
    suspend fun searchMovies(query: String): List<PartialMovie>
    suspend fun fetchFullMovie(movieId: Long): Movie?
}

class RealSearchRepository @Inject constructor(
    private val apiService: TmdbApiService
) : SearchRepository {

    override suspend fun searchMovies(
        query: String
    ): List<PartialMovie> = apiService.search(query).results.map { PartialMovie.from(it) }

    override suspend fun fetchFullMovie(movieId: Long): Movie? {
        val apiMovie = apiService.movie(movieId)
        return Movie.from(apiMovie)
    }
}
