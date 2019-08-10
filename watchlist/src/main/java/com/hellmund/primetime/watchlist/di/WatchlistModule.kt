package com.hellmund.primetime.watchlist.di

import com.hellmund.primetime.data.RealWatchlistRepository
import com.hellmund.primetime.data.WatchlistRepository
import dagger.Binds
import dagger.Module

@Module
interface WatchlistModule {

    @Binds
    fun bindWatchlistRepository(impl: RealWatchlistRepository): WatchlistRepository

}
