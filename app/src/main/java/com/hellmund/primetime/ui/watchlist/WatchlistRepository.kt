package com.hellmund.primetime.ui.watchlist

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.data.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.flow.asFlow
import javax.inject.Inject

interface WatchlistRepository {
    suspend fun getAll(): Flow<List<WatchlistMovie>>
    suspend fun getReleases(): List<WatchlistMovie>
    suspend fun count(movieId: Int): Int
    suspend fun store(movie: Movie)
    suspend fun store(watchlistMovie: WatchlistMovie)
    suspend fun remove(movieId: Int)
}

class RealWatchlistRepository @Inject constructor(
    private val database: AppDatabase
) : WatchlistRepository {

    @ExperimentalCoroutinesApi
    @FlowPreview
    @ObsoleteCoroutinesApi
    override suspend fun getAll() = database.watchlistDao().getAll().asFlow()

    override suspend fun getReleases() = database.watchlistDao().releases()

    override suspend fun count(movieId: Int) = database.watchlistDao().count(movieId)

    override suspend fun store(movie: Movie) {
        store(WatchlistMovie.from(movie))
    }

    override suspend fun store(watchlistMovie: WatchlistMovie) {
        database.watchlistDao().store(watchlistMovie)
    }

    override suspend fun remove(movieId: Int) {
        database.watchlistDao().delete(movieId)
    }

}
