package com.hellmund.primetime.data.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
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