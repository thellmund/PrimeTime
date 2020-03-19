package com.hellmund.primetime.data.database

import com.hellmund.primetime.data.Database
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface HistoryDao {
    fun observeAll(): Flow<List<HistoryMovie>>
    suspend fun getLiked(): List<HistoryMovie>
    suspend fun count(movieId: Long): Int
    suspend fun store(vararg movies: HistoryMovie)
    suspend fun updateRating(movie: HistoryMovie, rating: Rating)
    suspend fun delete(id: Long)
}

class RealHistoryDao @Inject constructor(
    database: Database
) : HistoryDao {

    private val queries = database.historyMovieQueries

    override fun observeAll(): Flow<List<HistoryMovie>> = queries.getAll().asFlow().mapToList()

    override suspend fun getLiked(): List<HistoryMovie> = queries.getLiked().executeAsList()

    override suspend fun count(
        movieId: Long
    ): Int = queries.getCount(movieId).executeAsOne().toInt()

    override suspend fun store(vararg movies: HistoryMovie) {
        for (movie in movies) {
            queries.store(movie.id, movie.title, movie.rating, movie.timestamp)
        }
    }

    override suspend fun updateRating(movie: HistoryMovie, rating: Rating) {
        queries.updateRating(id = movie.id, rating = rating)
    }

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}
