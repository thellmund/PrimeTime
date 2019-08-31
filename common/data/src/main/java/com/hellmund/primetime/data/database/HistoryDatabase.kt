package com.hellmund.primetime.data.database

import com.hellmund.primetime.data.Database
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface HistoryDatabase {
    suspend fun getAll(): List<HistoryMovie>
    fun observeAll(): Flow<List<HistoryMovie>>
    suspend fun getLiked(): List<HistoryMovie>
    suspend fun count(movieId: Int): Int
    suspend fun store(vararg movies: HistoryMovie)
    suspend fun updateRating(movie: HistoryMovie, rating: Rating)
    suspend fun delete(id: Int)
}

class RealHistoryDatabase @Inject constructor(
    database: Database
) : HistoryDatabase {

    private val queries = database.historyMovieQueries

    override suspend fun getAll(): List<HistoryMovie> = queries.getAll().executeAsList()

    override fun observeAll(): Flow<List<HistoryMovie>> = queries.getAll().asFlow().mapToList()

    override suspend fun getLiked(): List<HistoryMovie> = queries.getLiked().executeAsList()

    override suspend fun count(
        movieId: Int
    ): Int = queries.getCount(movieId.toLong()).executeAsOne().toInt()

    override suspend fun store(vararg movies: HistoryMovie) {
        for (movie in movies) {
            queries.store(movie.id, movie.title, movie.rating, movie.timestamp)
        }
    }

    override suspend fun updateRating(movie: HistoryMovie, rating: Rating) {
        queries.updateRating(id = movie.id, rating = rating)
    }

    override suspend fun delete(id: Int) {
        queries.delete(id.toLong())
    }

}
