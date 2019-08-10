package com.hellmund.primetime.watchlist.di

import com.hellmund.primetime.data.repositories.RealWatchlistRepository
import com.hellmund.primetime.data.repositories.WatchlistRepository
import dagger.Binds
import dagger.Module
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@Module
interface WatchlistModule {

    @Binds
    fun bindWatchlistRepository(impl: RealWatchlistRepository): WatchlistRepository

}
