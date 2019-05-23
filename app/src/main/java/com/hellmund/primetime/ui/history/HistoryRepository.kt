package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.HistoryMovie
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface HistoryRepository {
    fun getAll(): Maybe<List<HistoryMovie>>
    fun getLiked(): Maybe<List<HistoryMovie>>
    fun count(movieId: Int): Maybe<Int>
    fun store(vararg historyMovie: HistoryMovie): Completable
    fun remove(movieId: Int): Completable
}

class RealHistoryRepository @Inject constructor(
        private val database: AppDatabase
) : HistoryRepository {

    override fun getAll(): Maybe<List<HistoryMovie>> = database.historyDao()
            .getAll()
            .subscribeOn(Schedulers.io())

    override fun getLiked(): Maybe<List<HistoryMovie>> = database.historyDao()
            .getLiked()
            .subscribeOn(Schedulers.io())

    override fun count(movieId: Int): Maybe<Int> = database.historyDao()
            .count(movieId)
            .subscribeOn(Schedulers.io())

    override fun store(vararg historyMovie: HistoryMovie): Completable {
        return database.historyDao()
                .store(*historyMovie)
                .subscribeOn(Schedulers.io())
    }

    override fun remove(movieId: Int): Completable {
        return database.historyDao()
                .delete(movieId)
                .subscribeOn(Schedulers.io())
    }

}
