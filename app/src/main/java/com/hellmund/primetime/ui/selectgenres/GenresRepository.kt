package com.hellmund.primetime.ui.selectgenres

import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.database.AppDatabase
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
    private val apiService: ApiService,
    private val database: AppDatabase
) : GenresRepository {

    override suspend fun getAll(): List<Genre> {
        return database.genreDao().getAll()
    }

    override suspend fun getPreferredGenres() = database.genreDao().getPreferredGenres()

    override suspend fun getExcludedGenres() = database.genreDao().getExcludedGenres()

    override suspend fun fetchGenres(): List<Genre> {
        val genres = apiService.genres()
        return genres.genres.map { Genre(it.id, it.name) }
    }

    override suspend fun getGenre(
        genreId: String
    ): Genre = database.genreDao().getGenre(genreId.toInt())

    override suspend fun getGenreByName(name: String) = database.genreDao().getGenre(name)

    override suspend fun getGenres(genreIds: Set<String>): List<Genre> {
        return genreIds.map { database.genreDao().getGenre(it.toInt()) }
    }

    override suspend fun storeGenres(genres: List<Genre>) {
        database.genreDao().store(*genres.toTypedArray())
    }

}
