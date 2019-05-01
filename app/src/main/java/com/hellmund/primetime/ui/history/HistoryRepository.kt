package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.HistoryMovie
import io.reactivex.Maybe
import javax.inject.Inject

class HistoryRepository @Inject constructor(
        private val database: AppDatabase
) {

    fun getAll(): Maybe<List<HistoryMovie>> {
        return database.historyDao().getAll()
    }

    fun count(movieId: Int): Maybe<Int> {
        return database.historyDao().count(movieId)
    }

    fun store(vararg historyMovie: HistoryMovie) {
        database.historyDao().store(*historyMovie)
    }

}
