package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.HistoryMovie
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HistoryRepository @Inject constructor(
        private val database: AppDatabase
) {

    fun getAll(): Maybe<List<HistoryMovie>> = database.historyDao()
            .getAll()
            .subscribeOn(Schedulers.io())

    fun getLiked(): Maybe<List<HistoryMovie>> = database.historyDao()
            .getLiked()
            .subscribeOn(Schedulers.io())

    fun count(movieId: Int): Maybe<Int> = database.historyDao()
            .count(movieId)
            .subscribeOn(Schedulers.io())

    fun store(vararg historyMovie: HistoryMovie): Completable {
        return database.historyDao()
                .store(*historyMovie)
                .subscribeOn(Schedulers.io())
    }

    fun remove(movieId: Int): Completable {
        return database.historyDao()
                .delete(movieId)
                .subscribeOn(Schedulers.io())
    }

}
