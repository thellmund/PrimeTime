package com.hellmund.primetime.ui.watchlist.di

import com.hellmund.primetime.ui.watchlist.WatchlistMovieViewEntity
import com.hellmund.primetime.ui.watchlist.details.WatchlistMovieFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface WatchlistMovieComponent {

    fun inject(watchlistMovieFragment: WatchlistMovieFragment)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun movie(watchlistMovieViewEntity: WatchlistMovieViewEntity): Builder

        fun build(): WatchlistMovieComponent

    }

}
