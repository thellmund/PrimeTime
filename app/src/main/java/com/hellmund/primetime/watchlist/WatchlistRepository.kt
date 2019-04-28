package com.hellmund.primetime.watchlist

import com.hellmund.primetime.database.AppDatabase
import com.hellmund.primetime.database.WatchlistMovie
import com.hellmund.primetime.model2.ApiMovie
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WatchlistRepository @Inject constructor(
        private val database: AppDatabase
) {

    fun getAll(): Maybe<List<WatchlistMovie>> {
        return database.watchlistDao().getAll()
    }

    fun get(movieId: Int): Maybe<WatchlistMovie> {
        return database.watchlistDao().get(movieId)
    }

    fun count(movieId: Int): Maybe<Int> {
        return database.watchlistDao().count(movieId)
    }

    fun store(movie: ApiMovie): Completable {
        val watchlistMovie = WatchlistMovie.from(movie)
        return Completable.fromCallable { database.watchlistDao().store(watchlistMovie) }
    }

    fun remove(movieId: Int): Completable {
        return get(movieId)
                .subscribeOn(Schedulers.io())
                .flatMapCompletable {
                    Completable
                            .fromCallable { database.watchlistDao().delete(it) }
                            .subscribeOn(Schedulers.io())
                }
    }

    fun remove(movie: WatchlistMovie): Completable {
        return Completable
                .fromCallable { database.watchlistDao().delete(movie) }
                .subscribeOn(Schedulers.io())
    }

}
