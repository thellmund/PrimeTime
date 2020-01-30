package com.hellmund.primetime.onboarding.domain

import com.hellmund.api.TmdbApiService
import com.hellmund.api.model.ApiSample
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.repositories.HistoryRepository
import org.threeten.bp.LocalDate
import java.util.Date
import javax.inject.Inject

data class Sample(
    val id: Long,
    val title: String,
    val posterUrl: String,
    val popularity: Double,
    val releaseDate: Date?,
    val isSelected: Boolean = false
)

interface SamplesRepository {
    suspend fun fetch(genres: List<Genre>, page: Int): List<Sample>
    suspend fun store(movies: List<HistoryMovie>)
}

class RealSamplesRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val historyRepository: HistoryRepository,
    private val enricher: SampleEnricher
) : SamplesRepository {

    override suspend fun fetch(
        genres: List<Genre>,
        page: Int
    ): List<Sample> {
        val moviesPerGenre = 30 / genres.size
        val endYear = LocalDate.now().minusYears(1).year
        val startYear = endYear - 1
        val years = startYear..endYear

        val results = mutableListOf<List<ApiSample>>()

        for (genre in genres) {
            val movieResults = mutableListOf<ApiSample>()
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
            .map { enricher.enrich(it) }
    }

    override suspend fun store(movies: List<HistoryMovie>) {
        historyRepository.store(*movies.toTypedArray())
    }
}

class SampleEnricher @Inject constructor() {
    fun enrich(apiSample: ApiSample) = Sample(
        id = apiSample.id,
        title = apiSample.title,
        posterUrl = "https://image.tmdb.org/t/p/w500${apiSample.posterPath}",
        popularity = apiSample.popularity,
        releaseDate = apiSample.releaseDate
    )
}
