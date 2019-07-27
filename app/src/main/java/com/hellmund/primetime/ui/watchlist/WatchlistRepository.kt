package com.hellmund.primetime.ui.watchlist

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.data.model.Movie
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface WatchlistRepository {
    fun getAll(): Single<List<WatchlistMovie>>
    fun getReleases(): Single<List<WatchlistMovie>>
    fun get(movieId: Int): Maybe<WatchlistMovie>
    fun count(movieId: Int): Maybe<Int>
    fun store(movie: Movie): Completable
    fun store(watchlistMovie: WatchlistMovie): Completable
    fun remove(movieId: Int): Completable
}

class RealWatchlistRepository @Inject constructor(
    private val database: AppDatabase
) : WatchlistRepository {

    override fun getAll(): Single<List<WatchlistMovie>> {
        return database.watchlistDao().getAll().subscribeOn(Schedulers.io())
    }

    override fun getReleases(): Single<List<WatchlistMovie>> {
        return database.watchlistDao().releases().subscribeOn(Schedulers.io())
    }

    override fun get(movieId: Int): Maybe<WatchlistMovie> {
        return database.watchlistDao().get(movieId).subscribeOn(Schedulers.io())
    }

    override fun count(movieId: Int): Maybe<Int> {
        return database.watchlistDao().count(movieId).subscribeOn(Schedulers.io())
    }

    override fun store(movie: Movie): Completable {
        val watchlistMovie = WatchlistMovie.from(movie)
        return store(watchlistMovie)
    }

    override fun store(watchlistMovie: WatchlistMovie): Completable {
        return database.watchlistDao().store(watchlistMovie).subscribeOn(Schedulers.io())
    }

    override fun remove(movieId: Int): Completable {
        return database.watchlistDao().delete(movieId).subscribeOn(Schedulers.io())
    }

}
