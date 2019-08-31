package com.hellmund.primetime.data.repositories

import com.hellmund.primetime.data.database.WatchlistDatabase
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.WatchlistMovie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import javax.inject.Inject

interface WatchlistRepository {
    suspend fun getAll(): List<WatchlistMovie>
    suspend fun observeAll(): Flow<List<WatchlistMovie>>
    suspend fun getReleases(date: LocalDate): List<WatchlistMovie>
    suspend fun count(movieId: Int): Int
    suspend fun store(movie: Movie)
    suspend fun store(watchlistMovie: WatchlistMovie)
    suspend fun toggleNotification(movieId: Int)
    suspend fun remove(movieId: Int)
}

@ExperimentalCoroutinesApi
@FlowPreview
class RealWatchlistRepository @Inject constructor(
    private val database: WatchlistDatabase
) : WatchlistRepository {

    override suspend fun getAll() = database.getAll()

    override suspend fun observeAll() = database.observeAll()

    override suspend fun getReleases(
        date: LocalDate
    ) = database.getReleases(
        start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        end = date.atStartOfDay(ZoneId.systemDefault())
            .withHour(23)
            .withMinute(59)
            .withSecond(59)
            .withNano(999_999)
            .toInstant().toEpochMilli()
    )

    override suspend fun count(movieId: Int) = database.count(movieId)

    override suspend fun store(movie: Movie) {
        store(
            WatchlistMovie.Impl(
                id = movie.id.toLong(),
                title = movie.title,
                posterUrl = movie.posterPath,
                description = movie.description,
                runtime = movie.runtime ?: -1, // TODO
                releaseDate = movie.releaseDate ?: LocalDate.now(),
                addedAt = LocalDateTime.now(),
                deleted = false,
                notificationsActivated = true
            )
        )
    }

    override suspend fun store(watchlistMovie: WatchlistMovie) {
        database.store(watchlistMovie)
    }

    override suspend fun toggleNotification(movieId: Int) {
        // TODO
    }

    override suspend fun remove(movieId: Int) {
        database.delete(movieId)
    }

}
