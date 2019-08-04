package com.hellmund.primetime.ui.watchlist.di

import com.hellmund.primetime.ui.watchlist.RealWatchlistRepository
import com.hellmund.primetime.ui.watchlist.WatchlistMovieViewEntity
import com.hellmund.primetime.ui.watchlist.WatchlistRepository
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@Module
interface WatchlistModule {

    @Binds
    fun bindWatchlistRepository(impl: RealWatchlistRepository): WatchlistRepository

}
