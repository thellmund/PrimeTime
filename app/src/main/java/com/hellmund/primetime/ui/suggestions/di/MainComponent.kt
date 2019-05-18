package com.hellmund.primetime.ui.suggestions.di

import com.hellmund.primetime.ui.suggestions.MainFragment
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface MainComponent {

    fun inject(mainFragment: MainFragment)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun type(recommendationsType: RecommendationsType): Builder

        fun build(): MainComponent

    }

}
