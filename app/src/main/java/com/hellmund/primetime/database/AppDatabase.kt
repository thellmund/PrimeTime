package com.hellmund.primetime.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.hellmund.primetime.model2.Genre

@Database(
        entities = [
            WatchlistMovie::class,
            HistoryMovie::class,
            Genre::class
        ],
        version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun genreDao(): GenreDao

    abstract fun historyDao(): HistoryDao

    abstract fun watchlistDao(): WatchlistDao

}
