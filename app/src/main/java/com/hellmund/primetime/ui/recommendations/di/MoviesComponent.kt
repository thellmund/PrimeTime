package com.hellmund.primetime.ui.recommendations.di

import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.ui.recommendations.MoviesRepository
import com.hellmund.primetime.ui.recommendations.RealMoviesRepository
import com.hellmund.primetime.ui.recommendations.HomeFragment
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
