package com.hellmund.primetime.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hellmund.primetime.utils.endOfDay
import com.hellmund.primetime.utils.startOfDay
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_movies ORDER BY timestamp")
    suspend fun getAll(): List<WatchlistMovie>

    @Query("SELECT * FROM watchlist_movies ORDER BY timestamp")
    fun getAllRx(): Flowable<List<WatchlistMovie>>

    @Query("SELECT * FROM watchlist_movies WHERE id = :movieId")
    fun get(movieId: Int): Maybe<WatchlistMovie>

    @Query("SELECT * FROM watchlist_movies WHERE releaseDate BETWEEN :start AND :end")
    fun releases(
            start: Long = startOfDay,
            end: Long = endOfDay
    ): Single<List<WatchlistMovie>>

    @Query("SELECT COUNT(*) FROM watchlist_movies WHERE id = :movieId")
    suspend fun count(movieId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun store(vararg movie: WatchlistMovie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeRx(vararg movie: WatchlistMovie): Completable

    @Query("DELETE FROM watchlist_movies WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM watchlist_movies WHERE id = :id")
    fun deleteRx(id: Int): Completable

}
