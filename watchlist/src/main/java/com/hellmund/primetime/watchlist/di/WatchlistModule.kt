package com.hellmund.primetime.watchlist.di

import com.hellmund.primetime.data.repositories.RealWatchlistRepository
import com.hellmund.primetime.data.repositories.WatchlistRepository
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@Module
interface WatchlistModule {

    @ContributesAndroidInjector
    fun contributeFragmentInjector(): WatchlistFragment

    @Binds
    fun bindWatchlistRepository(impl: RealWatchlistRepository): WatchlistRepository

}
