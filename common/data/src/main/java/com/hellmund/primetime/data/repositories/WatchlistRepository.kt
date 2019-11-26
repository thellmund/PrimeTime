package com.hellmund.primetime.data.repositories

import com.hellmund.primetime.data.database.WatchlistDao
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.WatchlistMovie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

interface WatchlistRepository {
    suspend fun getAll(): List<WatchlistMovie>
    suspend fun observeAll(): Flow<List<WatchlistMovie>>
    suspend fun getReleases(date: LocalDate): List<WatchlistMovie>
    suspend fun count(movieId: Long): Int
    suspend fun store(movie: Movie)
    suspend fun store(watchlistMovie: WatchlistMovie)
    suspend fun toggleNotification(watchlistMovie: WatchlistMovie)
    suspend fun remove(movieId: Long)
}

@ExperimentalCoroutinesApi
@FlowPreview
class RealWatchlistRepository @Inject constructor(
    private val dao: WatchlistDao
) : WatchlistRepository {

    override suspend fun getAll() = dao.getAll()

    override suspend fun observeAll() = dao.observeAll()

    override suspend fun getReleases(
        date: LocalDate
    ): List<WatchlistMovie> = getAll().filter { it.releaseDate.isEqual(date) }

    override suspend fun count(movieId: Long) = dao.count(movieId)

    override suspend fun store(movie: Movie) {
        store(
            WatchlistMovie.Impl(
                id = movie.id,
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
        dao.store(watchlistMovie)
    }

    override suspend fun toggleNotification(watchlistMovie: WatchlistMovie) {
        dao.toggleNotification(watchlistMovie.id, isActive = watchlistMovie.notificationsActivated.not())
    }

    override suspend fun remove(movieId: Long) {
        dao.delete(movieId)
    }
}
