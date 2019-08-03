package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.utils.asFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface HistoryRepository {
    val all: List<HistoryMovie>
    suspend fun getAll(): Flow<List<HistoryMovie>>
    suspend fun getLiked(): List<HistoryMovie>
    suspend fun count(movieId: Int): Int
    suspend fun store(vararg historyMovie: HistoryMovie)
    suspend fun remove(movieId: Int)
}

class RealHistoryRepository @Inject constructor(
    private val database: AppDatabase
) : HistoryRepository {

    override val all: List<HistoryMovie>
        get() = database.historyDao().getAll().blockingFirst()

    @FlowPreview
    @ObsoleteCoroutinesApi
    override suspend fun getAll(): Flow<List<HistoryMovie>> {
        return database.historyDao().getAll().asFlow()
    }

    override suspend fun getLiked(): List<HistoryMovie> = database.historyDao().getLiked()

    override suspend fun count(movieId: Int) = database.historyDao().count(movieId)

    override suspend fun store(vararg historyMovie: HistoryMovie) {
        return database.historyDao().store(*historyMovie)
    }

    override suspend fun remove(movieId: Int) {
        database.historyDao().delete(movieId)
    }

}
