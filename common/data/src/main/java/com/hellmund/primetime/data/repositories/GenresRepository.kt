package com.hellmund.primetime.data.repositories

import com.hellmund.api.TmdbApiService
import com.hellmund.primetime.data.database.GenreDatabase
import com.hellmund.primetime.data.model.Genre
import javax.inject.Inject

interface GenresRepository {
    suspend fun getAll(): List<Genre>
    suspend fun getPreferredGenres(): List<Genre>
    suspend fun getExcludedGenres(): List<Genre>
    suspend fun fetchGenres(): List<Genre>
    suspend fun getGenre(genreId: String): Genre
    suspend fun getGenreByName(name: String): Genre
    suspend fun getGenres(genreIds: Set<String>): List<Genre>
    suspend fun storeGenres(genres: List<Genre>)
}

class RealGenresRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val database: GenreDatabase
) : GenresRepository {

    override suspend fun getAll(): List<Genre> = database.getAll()

    override suspend fun getPreferredGenres() = database.getPreferredGenres()

    override suspend fun getExcludedGenres() = database.getExcludedGenres()

    override suspend fun fetchGenres(): List<Genre> {
        val genres = apiService.genres()
        return genres.genres.map {
            Genre.Impl(it.id.toLong(), it.name, isPreferred = false, isExcluded = false)
        }
    }

    override suspend fun getGenre(
        genreId: String
    ): Genre = database.getGenre(genreId.toInt())

    override suspend fun getGenreByName(name: String) = database.getGenre(name)

    override suspend fun getGenres(genreIds: Set<String>): List<Genre> {
        return genreIds.map { database.getGenre(it.toInt()) }
    }

    override suspend fun storeGenres(genres: List<Genre>) {
        database.store(genres)
    }

}
