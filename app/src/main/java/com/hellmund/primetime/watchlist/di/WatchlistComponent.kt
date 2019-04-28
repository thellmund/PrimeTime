package com.hellmund.primetime.watchlist.di

import com.hellmund.primetime.database.AppDatabase
import com.hellmund.primetime.watchlist.WatchlistRepository
import dagger.Module
import dagger.Provides

/*@Subcomponent(modules = [WatchlistModule::class])
interface WatchlistComponent {
    fun inject(watchlistFragment: WatchlistFragment)
}*/

@Module
class WatchlistModule {

    @Provides
    fun provideWatchlistRepository(
            database: AppDatabase
    ): WatchlistRepository = WatchlistRepository(database)

}
