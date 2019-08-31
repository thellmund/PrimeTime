package com.hellmund.primetime.data.repositories

import com.hellmund.primetime.data.database.HistoryDatabase
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface HistoryRepository {
    suspend fun getAll(): List<HistoryMovie>
    suspend fun observeAll(): Flow<List<HistoryMovie>>
    suspend fun getLiked(): List<HistoryMovie>
    suspend fun count(movieId: Int): Int
    suspend fun store(vararg historyMovie: HistoryMovie)
    suspend fun updateRating(historyMovie: HistoryMovie, rating: Rating)
    suspend fun remove(movieId: Int)
}

@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
class RealHistoryRepository @Inject constructor(
    private val dao: HistoryDatabase
) : HistoryRepository {

    override suspend fun getAll(): List<HistoryMovie> = dao.getAll()

    override suspend fun observeAll(): Flow<List<HistoryMovie>> = dao.observeAll()

    override suspend fun getLiked(): List<HistoryMovie> = dao.getLiked()

    override suspend fun count(movieId: Int) = dao.count(movieId)

    override suspend fun store(vararg historyMovie: HistoryMovie) {
        dao.store(*historyMovie)
    }

    override suspend fun updateRating(historyMovie: HistoryMovie, rating: Rating) {
        dao.store()
    }

    override suspend fun remove(movieId: Int) {
        dao.delete(movieId)
    }

}
