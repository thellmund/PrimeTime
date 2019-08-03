package com.hellmund.primetime.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hellmund.primetime.data.model.Genre

@Dao
interface GenreDao {

    @Query("SELECT * FROM genres ORDER BY name")
    suspend fun getAll(): List<Genre>

    @Query("SELECT * FROM genres WHERE isPreferred = 1 ORDER BY name")
    suspend fun getPreferredGenres(): List<Genre>

    @Query("SELECT * FROM genres WHERE isExcluded = 1 ORDER BY name")
    suspend fun getExcludedGenres(): List<Genre>

    @Query("SELECT * FROM genres WHERE id = :id")
    suspend fun getGenre(id: Int): Genre

    @Query("SELECT * FROM genres WHERE name = :name")
    suspend fun getGenre(name: String): Genre

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun store(vararg genre: Genre)

}
