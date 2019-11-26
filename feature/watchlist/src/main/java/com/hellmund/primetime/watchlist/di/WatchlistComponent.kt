package com.hellmund.primetime.watchlist.di

import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import dagger.Component

@Component(
    dependencies = [CoreComponent::class]
)
@ActivityScope
interface WatchlistComponent {
    fun inject(watchlistFragment: WatchlistFragment)

    @Component.Builder
    interface Builder {
        fun core(coreComponent: CoreComponent): Builder
        fun build(): WatchlistComponent
    }
}
