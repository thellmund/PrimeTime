package com.hellmund.primetime.database

import android.arch.persistence.room.*
import com.hellmund.primetime.model2.Genre
import io.reactivex.Maybe

@Dao
interface GenreDao {

    @Query("SELECT * FROM genres")
    fun getAll(): Maybe<List<Genre>>

    @Query("SELECT * FROM genres WHERE isPreferred = 1")
    fun getPreferredGenres(): Maybe<List<Genre>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(vararg genre: Genre)

    @Delete
    fun delete(genre: Genre)

}
