package com.hellmund.primetime.ui.selectmovies

import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Sample
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.Calendar.YEAR
import javax.inject.Inject

class SelectMoviesRepository @Inject constructor(
        private val apiService: ApiService,
        private val historyRepository: HistoryRepository
) {

    fun fetch(genres: List<Genre>): Observable<List<Sample>> {
        return Observable
                .fromCallable { fetchSync(genres) }
                .subscribeOn(Schedulers.io())
    }

    private fun fetchSync(genres: List<Genre>): List<Sample> {
        val moviesPerGenre = 30 / genres.size
        val currentYear = Calendar.getInstance().get(YEAR)
        val startYear = currentYear - 4
        val years = startYear..currentYear

        val results = mutableListOf<List<Sample>>()

        for (genre in genres) {
            val movieResults = mutableListOf<Sample>()
            for (year in years) {
                movieResults += apiService
                        .discoverMovies(withGenres = genre.id, releaseYear = year)
                        .map { it.results }
                        .blockingFirst()
            }
            results += movieResults.subList(0, moviesPerGenre)
        }

        return results.flatten().toSet().toList()
    }

    fun store(movies: List<HistoryMovie>) {
        historyRepository.store(*movies.toTypedArray())
    }

}
