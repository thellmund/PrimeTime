package com.hellmund.primetime.data.database

import com.hellmund.primetime.data.Database
import com.hellmund.primetime.data.model.Genre
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface GenreDao {
    suspend fun getAll(): List<Genre>
    suspend fun observePreferredGenres(): Flow<List<Genre>>
    suspend fun getPreferredGenres(): List<Genre>
    suspend fun getExcludedGenres(): List<Genre>
    suspend fun getGenreById(id: Long): Genre
    suspend fun getGenreByName(name: String): Genre
    suspend fun store(genres: List<Genre>)
}

class RealGenreDao @Inject constructor(
    database: Database
) : GenreDao {

    private val queries = database.genreQueries

    override suspend fun getAll(): List<Genre> = queries.getAll().executeAsList()

    override suspend fun observePreferredGenres(): Flow<List<Genre>> {
        return queries.getPreferredGenres().asFlow().mapToList()
    }

    override suspend fun getPreferredGenres() = queries.getPreferredGenres().executeAsList()

    override suspend fun getExcludedGenres() = queries.getExcludedGenres().executeAsList()

    override suspend fun getGenreById(id: Long): Genre = queries.getGenre(id).executeAsOne()

    override suspend fun getGenreByName(
        name: String
    ): Genre = queries.getGenreByName(name).executeAsOne()

    override suspend fun store(genres: List<Genre>) {
        for (genre in genres) {
            queries.store(
                id = genre.id,
                name = genre.name,
                isPreferred = genre.isPreferred,
                isExcluded = genre.isExcluded
            )
        }
    }
}
