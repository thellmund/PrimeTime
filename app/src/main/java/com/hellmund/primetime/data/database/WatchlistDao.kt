package com.hellmund.primetime.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hellmund.primetime.utils.endOfDay
import com.hellmund.primetime.utils.startOfDay
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_movies ORDER BY timestamp")
    fun getAll(): Single<List<WatchlistMovie>>

    @Query("SELECT * FROM watchlist_movies WHERE id = :movieId")
    fun get(movieId: Int): Maybe<WatchlistMovie>

    @Query("SELECT * FROM watchlist_movies WHERE releaseDate BETWEEN :start AND :end")
    fun releases(
            start: Long = startOfDay,
            end: Long = endOfDay
    ): Single<List<WatchlistMovie>>

    @Query("SELECT COUNT(*) FROM watchlist_movies WHERE id = :movieId")
    fun count(movieId: Int): Maybe<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(vararg movie: WatchlistMovie)

    @Query("DELETE FROM watchlist_movies WHERE id = :id")
    fun delete(id: Int)

}
