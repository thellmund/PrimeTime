package com.hellmund.primetime.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.WatchlistMovie

@Database(entities = [
    WatchlistMovie::class,
    HistoryMovie::class,
    Genre::class
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun genreDao(): GenreDao

    abstract fun historyDao(): HistoryDao

    abstract fun watchlistDao(): WatchlistDao

}
