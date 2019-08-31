package com.hellmund.primetime.data.di

import android.content.Context
import com.hellmund.primetime.data.Database
import com.hellmund.primetime.data.database.DateColumnAdapter
import com.hellmund.primetime.data.database.GenreDao
import com.hellmund.primetime.data.database.HistoryDao
import com.hellmund.primetime.data.database.RatingColumnAdapter
import com.hellmund.primetime.data.database.RealGenreDao
import com.hellmund.primetime.data.database.RealHistoryDao
import com.hellmund.primetime.data.database.RealWatchlistDao
import com.hellmund.primetime.data.database.TimestampColumnAdapter
import com.hellmund.primetime.data.database.WatchlistDao
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.WatchlistMovie
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class DatabaseModule {

    @Binds
    abstract fun bindGenreDao(impl: RealGenreDao): GenreDao

    @Binds
    abstract fun bindHistoryMovieDao(impl: RealHistoryDao): HistoryDao

    @Binds
    abstract fun bindWatchlistMovieDao(impl: RealWatchlistDao): WatchlistDao

    @Module
    companion object {

        @JvmStatic
        @Provides
        @Singleton
        fun provideSqliteDriver(
            context: Context
        ): SqlDriver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context.applicationContext,
            name = "history_movies.db"
        )

        @JvmStatic
        @Provides
        @Singleton
        fun provideDatabase(
            sqlDriver: SqlDriver,
            ratingColumnAdapter: RatingColumnAdapter,
            timestampColumnAdapter: TimestampColumnAdapter,
            dateColumnAdapter: DateColumnAdapter
        ) = Database(
            driver = sqlDriver,
            historyMovieAdapter = HistoryMovie.Adapter(
                ratingAdapter = ratingColumnAdapter,
                timestampAdapter = timestampColumnAdapter
            ),
            watchlistMovieAdapter = WatchlistMovie.Adapter(
                releaseDateAdapter = dateColumnAdapter,
                addedAtAdapter = timestampColumnAdapter
            )
        )

    }

}
