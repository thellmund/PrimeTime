package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.HistoryMovie
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface HistoryRepository {
    fun getAll(): Maybe<List<HistoryMovie>>
    suspend fun getLiked(): List<HistoryMovie>
    fun getLikedRx(): Maybe<List<HistoryMovie>>
    suspend fun count(movieId: Int): Int
    fun storeRx(vararg historyMovie: HistoryMovie): Completable
    suspend fun store(vararg historyMovie: HistoryMovie)
    suspend fun remove(movieId: Int)
}

class RealHistoryRepository @Inject constructor(
        private val database: AppDatabase
) : HistoryRepository {

    override fun getAll(): Maybe<List<HistoryMovie>> = database.historyDao()
            .getAll()
            .subscribeOn(Schedulers.io())

    override suspend fun getLiked(): List<HistoryMovie> = database.historyDao().getLiked()

    override fun getLikedRx(): Maybe<List<HistoryMovie>> = database.historyDao()
        .getLikedRx()
        .subscribeOn(Schedulers.io())

    override suspend fun count(movieId: Int) = database.historyDao().count(movieId)

    override fun storeRx(vararg historyMovie: HistoryMovie): Completable {
        return database.historyDao()
            .storeRx(*historyMovie)
            .subscribeOn(Schedulers.io())
    }

    override suspend fun store(vararg historyMovie: HistoryMovie) {
        return database.historyDao().store(*historyMovie)
    }

    override suspend fun remove(movieId: Int) {
        database.historyDao().delete(movieId)
    }

}
