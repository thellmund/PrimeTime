package com.hellmund.primetime.data.repositories

import com.hellmund.primetime.data.database.HistoryDao
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun observeAll(): Flow<List<HistoryMovie>>
    suspend fun getLiked(): List<HistoryMovie>
    suspend fun count(movieId: Long): Int
    suspend fun contains(movieId: Long): Boolean
    suspend fun store(vararg historyMovie: HistoryMovie)
    suspend fun updateRating(historyMovie: HistoryMovie, rating: Rating)
    suspend fun remove(movieId: Long)
}

@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
class RealHistoryRepository @Inject constructor(
    private val dao: HistoryDao
) : HistoryRepository {

    override suspend fun observeAll(): Flow<List<HistoryMovie>> = dao.observeAll()

    override suspend fun getLiked(): List<HistoryMovie> = dao.getLiked()

    override suspend fun count(movieId: Long) = dao.count(movieId)

    override suspend fun contains(movieId: Long): Boolean = dao.count(movieId) > 0

    override suspend fun store(vararg historyMovie: HistoryMovie) {
        dao.store(*historyMovie)
    }

    override suspend fun updateRating(historyMovie: HistoryMovie, rating: Rating) {
        dao.store()
    }

    override suspend fun remove(movieId: Long) {
        dao.delete(movieId)
    }
}
