package com.hellmund.primetime.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_movies ORDER BY timestamp DESC")
    fun getAll(): Maybe<List<HistoryMovie>>

    @Query("SELECT * FROM history_movies WHERE rating = 1 ORDER BY timestamp DESC")
    suspend fun getLiked(): List<HistoryMovie>

    @Query("SELECT * FROM history_movies WHERE rating = 1 ORDER BY timestamp DESC")
    fun getLikedRx(): Maybe<List<HistoryMovie>>

    @Query("SELECT COUNT(*) FROM history_movies WHERE id = :movieId")
    suspend fun count(movieId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun store(vararg movie: HistoryMovie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeRx(vararg movie: HistoryMovie): Completable

    @Query("DELETE FROM history_movies WHERE id = :id")
    suspend fun delete(id: Int)

}