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
        return database.watchlistDao().getAll()
    }

    fun getReleases(): Single<List<WatchlistMovie>> {
        return database.watchlistDao().releases()
    }

    fun get(movieId: Int): Maybe<WatchlistMovie> {
        return database.watchlistDao().get(movieId)
    }

    fun count(movieId: Int): Maybe<Int> {
        return database.watchlistDao().count(movieId)
    }

    fun store(movie: Movie): Completable {
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
