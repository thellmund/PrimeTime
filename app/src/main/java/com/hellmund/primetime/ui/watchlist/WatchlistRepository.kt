package com.hellmund.primetime.ui.watchlist

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.data.model.Movie
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WatchlistRepository @Inject constructor(
        private val database: AppDatabase
) {

    fun getAll(): Single<List<WatchlistMovie>> {
        return database.watchlistDao().getAll().subscribeOn(Schedulers.io())
    }

    fun getReleases(): Single<List<WatchlistMovie>> {
        return database.watchlistDao().releases().subscribeOn(Schedulers.io())
    }

    fun get(movieId: Int): Maybe<WatchlistMovie> {
        return database.watchlistDao().get(movieId).subscribeOn(Schedulers.io())
    }

    fun count(movieId: Int): Maybe<Int> {
        return database.watchlistDao().count(movieId).subscribeOn(Schedulers.io())
    }

    fun store(movie: Movie): Completable {
        val watchlistMovie = WatchlistMovie.from(movie)
        return store(watchlistMovie)
    }

    fun store(watchlistMovie: WatchlistMovie): Completable {
        return database.watchlistDao().store(watchlistMovie).subscribeOn(Schedulers.io())
    }

    fun remove(movieId: Int): Completable {
        return database.watchlistDao().delete(movieId).subscribeOn(Schedulers.io())
    }

}
