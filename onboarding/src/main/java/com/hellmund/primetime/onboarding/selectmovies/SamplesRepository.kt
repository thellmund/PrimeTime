package com.hellmund.primetime.onboarding.selectmovies

import com.hellmund.api.Sample
import com.hellmund.api.TmdbApiService
import com.hellmund.primetime.data.HistoryRepository
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.HistoryMovie
import org.threeten.bp.LocalDate
import javax.inject.Inject

interface SamplesRepository {
    suspend fun fetch(genres: List<Genre>, page: Int): List<Sample>
    suspend fun store(movies: List<HistoryMovie>)
}

class RealSamplesRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val historyRepository: HistoryRepository
) : SamplesRepository {

    override suspend fun fetch(
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
                val response = apiService.discoverMovies(
                    genre = genre.id, releaseYear = year, page = page)
                movieResults += response.results
            }
            results += movieResults.subList(0, moviesPerGenre)
        }

        return results
            .flatten()
            .toSet()
            .toList()
    }

    override suspend fun store(movies: List<HistoryMovie>) {
        historyRepository.store(*movies.toTypedArray())
    }

}
