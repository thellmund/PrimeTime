package com.hellmund.primetime.history

import com.hellmund.primetime.database.AppDatabase
import com.hellmund.primetime.database.HistoryMovie
import io.reactivex.Maybe

class HistoryRepository(
        private val database: AppDatabase
) {

    fun getAll(): Maybe<List<HistoryMovie>> {
        return database.historyDao().getAll()
    }

    fun store(vararg historyMovie: HistoryMovie) {
        database.historyDao().store(*historyMovie)
    }

}
