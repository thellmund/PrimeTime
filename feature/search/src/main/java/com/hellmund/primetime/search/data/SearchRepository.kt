package com.hellmund.primetime.search.data

import com.hellmund.api.TmdbApiService
import com.hellmund.primetime.data.model.Movie
import javax.inject.Inject

interface SearchRepository {
    suspend fun searchMovies(query: String): List<Movie>
}

class RealSearchRepository @Inject constructor(
    private val apiService: TmdbApiService
) : SearchRepository {

    override suspend fun searchMovies(
        query: String
    ): List<Movie> = apiService.search(query).results
        .filter { it.isValid }
        .map { Movie.from(it) }

}
