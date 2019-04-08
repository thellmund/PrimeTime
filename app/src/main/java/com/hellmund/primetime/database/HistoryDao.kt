package com.hellmund.primetime.database

import android.arch.persistence.room.*
import io.reactivex.Maybe

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_movies")
    fun getAll(): Maybe<List<HistoryMovie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(vararg movie: HistoryMovie)

    @Delete
    fun delete(movie: HistoryMovie)

}