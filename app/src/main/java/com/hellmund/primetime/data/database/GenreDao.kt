package com.hellmund.primetime.data.database

import android.arch.persistence.room.*
import com.hellmund.primetime.data.model.Genre
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface GenreDao {

    @Query("SELECT * FROM genres ORDER BY name")
    fun getAll(): Single<List<Genre>>

    @Query("SELECT * FROM genres WHERE isPreferred = 1 ORDER BY name")
    fun getPreferredGenres(): Single<List<Genre>>

    @Query("SELECT * FROM genres WHERE isPreferred = 0 ORDER BY name")
    fun getExcludedGenres(): Single<List<Genre>>

    @Query("SELECT * FROM genres WHERE id = :id")
    fun getGenre(id: Int): Maybe<Genre>

    @Query("SELECT * FROM genres WHERE name = :name")
    fun getGenre(name: String): Maybe<Genre>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(vararg genre: Genre)

    @Delete
    fun delete(genre: Genre)

}
