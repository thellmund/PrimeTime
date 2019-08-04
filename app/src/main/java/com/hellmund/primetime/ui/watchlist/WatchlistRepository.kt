package com.hellmund.primetime.ui.watchlist

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.data.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.flow.asFlow
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import javax.inject.Inject

interface WatchlistRepository {
    suspend fun getAll(): List<WatchlistMovie>
    suspend fun observeAll(): Flow<List<WatchlistMovie>>
    suspend fun getReleases(date: LocalDate): List<WatchlistMovie>
    suspend fun count(movieId: Int): Int
    suspend fun store(movie: Movie)
    suspend fun store(watchlistMovie: WatchlistMovie)
    suspend fun remove(movieId: Int)
}

@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
class RealWatchlistRepository @Inject constructor(
    private val database: AppDatabase
) : WatchlistRepository {

    override suspend fun getAll() = database.watchlistDao().getAll()

    override suspend fun observeAll() = database.watchlistDao().observeAll().asFlow()

    override suspend fun getReleases(
        date: LocalDate
    ) = database.watchlistDao().releases(
        start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        end = date.atStartOfDay(ZoneId.systemDefault())
            .withHour(23)
            .withMinute(59)
            .withSecond(59)
            .withNano(999_999)
            .toInstant().toEpochMilli()
    )

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
