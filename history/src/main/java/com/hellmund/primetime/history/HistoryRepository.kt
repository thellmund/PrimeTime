package com.hellmund.primetime.history

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.model.HistoryMovie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import javax.inject.Inject

interface HistoryRepository {
    suspend fun getAll(): List<HistoryMovie>
    suspend fun observeAll(): Flow<List<HistoryMovie>>
    suspend fun getLiked(): List<HistoryMovie>
    suspend fun count(movieId: Int): Int
    suspend fun store(vararg historyMovie: HistoryMovie)
    suspend fun remove(movieId: Int)
}

@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
class RealHistoryRepository @Inject constructor(
    private val database: AppDatabase
) : HistoryRepository {

    override suspend fun getAll(): List<HistoryMovie> {
        return database.historyDao().getAll()
    }

    override suspend fun observeAll(): Flow<List<HistoryMovie>> {
        return database.historyDao().observeAll().asFlow()
    }

    override suspend fun getLiked(): List<HistoryMovie> = database.historyDao().getLiked()

    override suspend fun count(movieId: Int) = database.historyDao().count(movieId)

    override suspend fun store(vararg historyMovie: HistoryMovie) {
        database.historyDao().store(*historyMovie)
    }

    override suspend fun remove(movieId: Int) {
        database.historyDao().delete(movieId)
    }

}
