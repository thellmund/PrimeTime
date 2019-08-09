package com.hellmund.primetime.ui.suggestions.di

import com.hellmund.primetime.ui.suggestions.HomeFragment
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.ui.suggestions.data.RealMoviesRepository
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@Subcomponent
interface MoviesComponent {

    fun inject(mainFragment: HomeFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance recommendationsType: RecommendationsType): MoviesComponent
    }

}

@Module
interface MoviesModule {

    @Binds
    fun bindMoviesRepository(impl: RealMoviesRepository): MoviesRepository

}
