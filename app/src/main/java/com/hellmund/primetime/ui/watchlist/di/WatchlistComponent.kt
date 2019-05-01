package com.hellmund.primetime.ui.watchlist.di

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.ui.watchlist.WatchlistRepository
import dagger.Module
import dagger.Provides

@Module
class WatchlistModule {

    @Provides
    fun provideWatchlistRepository(
            database: AppDatabase
    ): WatchlistRepository = WatchlistRepository(database)

}
