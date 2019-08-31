package com.hellmund.primetime.data.database

import com.hellmund.primetime.data.Database
import com.hellmund.primetime.data.model.WatchlistMovie
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface WatchlistDatabase {
    suspend fun getAll(): List<WatchlistMovie>
    fun observeAll(): Flow<List<WatchlistMovie>>
    suspend fun getReleases(start: Long, end: Long): List<WatchlistMovie>
    suspend fun count(movieId: Int): Int
    suspend fun store(vararg movies: WatchlistMovie)
    suspend fun delete(id: Int)
}

class RealWatchlistDatabase @Inject constructor(
    database: Database
) : WatchlistDatabase {

    private val queries = database.watchlistMovieQueries

    override suspend fun getAll(): List<WatchlistMovie> = queries.getAll().executeAsList()

    override fun observeAll(): Flow<List<WatchlistMovie>> = queries.getAll().asFlow().mapToList()

    override suspend fun getReleases(start: Long, end: Long): List<WatchlistMovie> {
        // TODO queries.getReleases(minTime = start, maxTime =  end)
        return emptyList()
    }

    override suspend fun count(
        movieId: Int
    ): Int = queries.getCount(movieId.toLong()).executeAsOne().toInt()

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

    override suspend fun delete(id: Int) {
        queries.delete(id.toLong())
    }

}
