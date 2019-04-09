package com.hellmund.primetime.database

import android.arch.persistence.room.*
import io.reactivex.Maybe

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_movies")
    fun getAll(): Maybe<List<WatchlistMovie>>

    @Query("SELECT * FROM watchlist_movies WHERE id = :movieId")
    fun get(movieId: Int): Maybe<WatchlistMovie>

    @Query("SELECT COUNT(*) FROM watchlist_movies WHERE id = :movieId")
    fun count(movieId: Int): Maybe<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(vararg movie: WatchlistMovie)

    @Delete
    fun delete(movie: WatchlistMovie)

}
