package com.hellmund.primetime.data.model

import com.hellmund.api.TmdbApiService
import com.hellmund.api.model.PartialApiMovie
import javax.inject.Inject

class MovieEnricher @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {

    suspend fun enrich(partialApiMovie: PartialApiMovie): Movie? {
        val apiMovie = tmdbApiService.movie(partialApiMovie.id)
        return Movie.from(apiMovie)
    }
}
