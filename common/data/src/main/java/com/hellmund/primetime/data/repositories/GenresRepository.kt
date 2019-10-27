package com.hellmund.primetime.data.repositories

import com.hellmund.api.TmdbApiService
import com.hellmund.primetime.data.database.GenreDao
import com.hellmund.primetime.data.model.Genre
import javax.inject.Inject

interface GenresRepository {
    suspend fun getAll(): List<Genre>
    suspend fun getPreferredGenres(): List<Genre>
    suspend fun getExcludedGenres(): List<Genre>
    suspend fun fetchGenres(): List<Genre>
    suspend fun getGenreById(genreId: Long): Genre
    suspend fun getGenreByName(name: String): Genre
    suspend fun getGenres(genreIds: Set<String>): List<Genre>
    suspend fun storeGenres(genres: List<Genre>)
}

class RealGenresRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val dao: GenreDao
) : GenresRepository {

    override suspend fun getAll(): List<Genre> = dao.getAll()

    override suspend fun getPreferredGenres() = dao.getPreferredGenres()

    override suspend fun getExcludedGenres() = dao.getExcludedGenres()

    override suspend fun fetchGenres(): List<Genre> = apiService
        .genres()
        .genres
        .map { Genre.Impl(it.id, it.name, isPreferred = false, isExcluded = false) }

    override suspend fun getGenreById(
        genreId: Long
    ): Genre = dao.getGenreById(genreId)

    override suspend fun getGenreByName(name: String) = dao.getGenreByName(name)

    override suspend fun getGenres(
        genreIds: Set<String>
    ): List<Genre> = genreIds.map { dao.getGenreByName(it) }

    override suspend fun storeGenres(genres: List<Genre>) {
        dao.store(genres)
    }

}
