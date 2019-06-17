package com.hellmund.primetime.ui.watchlist

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.data.model.Movie
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface WatchlistRepository {
    suspend fun getAll(): List<WatchlistMovie>
    fun getAllRx(): Flowable<List<WatchlistMovie>>
    fun getReleases(): Single<List<WatchlistMovie>>
    fun get(movieId: Int): Maybe<WatchlistMovie>
    suspend fun count(movieId: Int): Int
    fun store(movie: Movie): Completable
    suspend fun store(watchlistMovie: WatchlistMovie)
    fun storeRx(watchlistMovie: WatchlistMovie): Completable
    suspend fun remove(movieId: Int)
    fun removeRx(movieId: Int): Completable
}

class RealWatchlistRepository @Inject constructor(
        private val database: AppDatabase
) : WatchlistRepository {

    override suspend fun getAll() = database.watchlistDao().getAll()

    override fun getAllRx(): Flowable<List<WatchlistMovie>> {
        return database.watchlistDao().getAllRx().subscribeOn(Schedulers.io())
    }

    override fun getReleases(): Single<List<WatchlistMovie>> {
        return database.watchlistDao().releases().subscribeOn(Schedulers.io())
    }

    override fun get(movieId: Int): Maybe<WatchlistMovie> {
        return database.watchlistDao().get(movieId).subscribeOn(Schedulers.io())
    }

    override suspend fun count(movieId: Int) = database.watchlistDao().count(movieId)

    override fun store(movie: Movie): Completable {
        val watchlistMovie = WatchlistMovie.from(movie)
        return storeRx(watchlistMovie)
    }

    override suspend fun store(watchlistMovie: WatchlistMovie) {
        database.watchlistDao().store(watchlistMovie)
    }

    override fun storeRx(watchlistMovie: WatchlistMovie): Completable {
        return database.watchlistDao().storeRx(watchlistMovie).subscribeOn(Schedulers.io())
    }

    override suspend fun remove(movieId: Int) {
        database.watchlistDao().delete(movieId)
    }

    override fun removeRx(movieId: Int): Completable {
        return database.watchlistDao().deleteRx(movieId).subscribeOn(Schedulers.io())
    }

}
