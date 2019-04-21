package com.hellmund.primetime.database

import android.arch.persistence.room.*
import com.hellmund.primetime.model2.Genre
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface GenreDao {

    @Query("SELECT * FROM genres")
    fun getAll(): Single<List<Genre>>

    @Query("SELECT * FROM genres WHERE isPreferred = 1")
    fun getPreferredGenres(): Single<List<Genre>>

    @Query("SELECT * FROM genres WHERE isPreferred = 0")
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
