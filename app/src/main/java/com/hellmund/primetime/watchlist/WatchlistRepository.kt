package com.hellmund.primetime.watchlist

import com.hellmund.primetime.database.AppDatabase
import com.hellmund.primetime.database.WatchlistMovie
import io.reactivex.Maybe

class WatchlistRepository(
        private val database: AppDatabase
) {

    fun getAll(): Maybe<List<WatchlistMovie>> {
        return database.watchlistDao().getAll()
    }

}
