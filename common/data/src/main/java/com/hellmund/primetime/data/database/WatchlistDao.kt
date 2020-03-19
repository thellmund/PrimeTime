package com.hellmund.primetime.data.database

import com.hellmund.primetime.data.Database
import com.hellmund.primetime.data.model.WatchlistMovie
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface WatchlistDao {
    fun observeAll(): Flow<List<WatchlistMovie>>
    suspend fun count(movieId: Long): Int
    suspend fun store(vararg movies: WatchlistMovie)
    suspend fun toggleNotification(movieId: Long, isActive: Boolean)
    suspend fun delete(id: Long)
}

class RealWatchlistDao @Inject constructor(
    database: Database
) : WatchlistDao {

    private val queries = database.watchlistMovieQueries

    override fun observeAll(): Flow<List<WatchlistMovie>> = queries.getAll().asFlow().mapToList()

    override suspend fun count(
        movieId: Long
    ): Int = queries.getCount(movieId).executeAsOne().toInt()

    override suspend fun store(vararg movies: WatchlistMovie) {
        for (movie in movies) {
            queries.store(
                id = movie.id,
                title = movie.title,
                posterUrl = movie.posterUrl,
                description = movie.description,
                runtime = movie.runtime,
                releaseDate = movie.releaseDate,
                addedAt = movie.addedAt,
                deleted = movie.deleted,
                notificationsActivated = movie.notificationsActivated
            )
        }
    }

    override suspend fun toggleNotification(movieId: Long, isActive: Boolean) {
        queries.toggleNotification(isActive, movieId)
    }

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}
