package com.hellmund.primetime.history.di

import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.history.ui.HistoryFragment
import dagger.Component

@Component(
    dependencies = [CoreComponent::class]
)
@ActivityScope
interface HistoryComponent {
    fun inject(historyFragment: HistoryFragment)

    @Component.Builder
    interface Builder {
        fun core(coreComponent: CoreComponent): Builder
        fun build(): HistoryComponent
    }
}
