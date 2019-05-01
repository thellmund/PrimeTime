package com.hellmund.primetime.data.database

import android.arch.persistence.room.*
import io.reactivex.Maybe

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_movies")
    fun getAll(): Maybe<List<HistoryMovie>>

    @Query("SELECT COUNT(*) FROM history_movies WHERE id = :movieId")
    fun count(movieId: Int): Maybe<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(vararg movie: HistoryMovie)

    @Delete
    fun delete(movie: HistoryMovie)

}