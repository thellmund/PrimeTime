package com.hellmund.primetime.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Maybe

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_movies ORDER BY timestamp DESC")
    fun getAll(): Maybe<List<HistoryMovie>>

    @Query("SELECT COUNT(*) FROM history_movies WHERE id = :movieId")
    fun count(movieId: Int): Maybe<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(vararg movie: HistoryMovie)

    @Query("DELETE FROM history_movies WHERE id = :id")
    fun delete(id: Int)

}