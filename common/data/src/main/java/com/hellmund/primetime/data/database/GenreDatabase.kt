package com.hellmund.primetime.data.database

import com.hellmund.primetime.data.Database
import com.hellmund.primetime.data.model.Genre
import javax.inject.Inject

interface GenreDatabase {
    suspend fun getAll(): List<Genre>
    suspend fun getPreferredGenres(): List<Genre>
    suspend fun getExcludedGenres(): List<Genre>
    suspend fun getGenre(id: Int): Genre
    suspend fun getGenre(name: String): Genre
    suspend fun store(genres: List<Genre>)
}

class RealGenreDatabase @Inject constructor(
    database: Database
) : GenreDatabase {

    private val queries = database.genreQueries

    override suspend fun getAll(): List<Genre> = queries.getAll().executeAsList()

    override suspend fun getPreferredGenres() = queries.getPreferredGenres().executeAsList()

    override suspend fun getExcludedGenres() = queries.getExcludedGenres().executeAsList()

    override suspend fun getGenre(id: Int): Genre = queries.getGenre(id.toLong()).executeAsOne()

    override suspend fun getGenre(name: String): Genre = queries.getGenreByName(name).executeAsOne()

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
