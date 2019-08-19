package com.hellmund.primetime.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hellmund.primetime.data.model.WatchlistMovie
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_movies ORDER BY timestamp")
    suspend fun getAll(): List<WatchlistMovie>

    @Query("SELECT * FROM watchlist_movies ORDER BY timestamp")
    fun observeAll(): Flow<List<WatchlistMovie>>

    @Query("SELECT * FROM watchlist_movies WHERE releaseDate BETWEEN :start AND :end")
    suspend fun releases(
        start: Long,
        end: Long
    ): List<WatchlistMovie>

    @Query("SELECT COUNT(*) FROM watchlist_movies WHERE id = :movieId")
    suspend fun count(movieId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun store(vararg movie: WatchlistMovie)

    @Query("DELETE FROM watchlist_movies WHERE id = :id")
    suspend fun delete(id: Int)

}
