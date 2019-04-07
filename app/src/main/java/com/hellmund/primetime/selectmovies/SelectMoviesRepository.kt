package com.hellmund.primetime.selectmovies

import com.hellmund.primetime.api.ApiService
import com.hellmund.primetime.model2.Sample
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.Calendar.YEAR

class SelectMoviesRepository(
        private val apiService: ApiService
) {

    fun fetch(genres: List<String>): Observable<List<Sample>> {
        return Observable
                .fromCallable { fetchSync(genres) }
                .subscribeOn(Schedulers.io())
    }

    private fun fetchSync(genres: List<String>): List<Sample> {
        val moviesPerGenre = 30 / genres.size
        val currentYear = Calendar.getInstance().get(YEAR)
        val startYear = currentYear - 4
        val years = startYear..currentYear

        val results = mutableListOf<List<Sample>>()

        for (genre in genres) {
            val movieResults = mutableListOf<Sample>()
            for (year in years) {
                movieResults += apiService
                        .discoverMovies(withGenres = genre, releaseYear = year)
                        .map { it.results }
                        .blockingFirst()
            }
            results += movieResults.subList(0, moviesPerGenre)
        }

        return results.flatten().toSet().toList()
    }

}
