package com.hellmund.primetime.recommendations.di

import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.recommendations.data.MoviesRepository
import com.hellmund.primetime.recommendations.data.RealMoviesRepository
import com.hellmund.primetime.recommendations.ui.HomeFragment
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
