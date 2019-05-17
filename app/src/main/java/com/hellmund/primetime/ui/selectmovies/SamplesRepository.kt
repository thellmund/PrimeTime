package com.hellmund.primetime.ui.selectmovies

import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.history.HistoryRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate
import javax.inject.Inject

class SamplesRepository @Inject constructor(
        private val apiService: ApiService,
        private val historyRepository: HistoryRepository
) {

    fun fetch(
            genres: List<Genre>,
            page: Int
    ): Observable<List<Sample>> {
        return Observable
                .fromCallable { fetchSync(genres, page) }
                .subscribeOn(Schedulers.io())
    }

    private fun fetchSync(
            genres: List<Genre>,
            page: Int
    ): List<Sample> {
        val moviesPerGenre = 30 / genres.size
        val endYear = LocalDate.now().minusYears(1).year
        val startYear = endYear - 1
        val years = startYear..endYear

        val results = mutableListOf<List<Sample>>()

        for (genre in genres) {
            val movieResults = mutableListOf<Sample>()
            for (year in years) {
                movieResults += apiService
                        .discoverMovies(genre = genre.id, releaseYear = year, page = page)
                        .map { it.results }
                        .blockingFirst()
            }
            results += movieResults.subList(0, moviesPerGenre)
        }

        return results
                .flatten()
                .toSet()
                .toList()
    }

    fun store(movies: List<HistoryMovie>): Completable {
        return historyRepository
                .store(*movies.toTypedArray())
                .subscribeOn(Schedulers.io())
    }

}
