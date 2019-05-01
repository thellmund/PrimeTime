package com.hellmund.primetime.data.database

import android.arch.persistence.room.*
import com.hellmund.primetime.utils.DateUtils
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_movies")
    fun getAll(): Single<List<WatchlistMovie>>

    @Query("SELECT * FROM watchlist_movies WHERE id = :movieId")
    fun get(movieId: Int): Maybe<WatchlistMovie>

    @Query("SELECT * FROM watchlist_movies WHERE releaseDate BETWEEN :start AND :end")
    fun releases(
            start: Long = DateUtils.startOfDay().toEpochMilli(),
            end: Long = DateUtils.endOfDay().toEpochMilli()
    ): Single<List<WatchlistMovie>>

    @Query("SELECT COUNT(*) FROM watchlist_movies WHERE id = :movieId")
    fun count(movieId: Int): Maybe<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(vararg movie: WatchlistMovie)

    @Delete
    fun delete(movie: WatchlistMovie)

}
