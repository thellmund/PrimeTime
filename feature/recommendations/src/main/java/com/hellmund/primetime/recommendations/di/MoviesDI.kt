package com.hellmund.primetime.recommendations.di

import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.recommendations.data.MovieRankingProcessor
import com.hellmund.primetime.recommendations.data.MoviesRepository
import com.hellmund.primetime.recommendations.data.RealMovieRankingProcessor
import com.hellmund.primetime.recommendations.data.RealMoviesRepository
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.ui_common.MovieViewEntitiesMapper
import com.hellmund.primetime.ui_common.RealMovieViewEntitiesMapper
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    modules = [MoviesModule::class],
    dependencies = [CoreComponent::class]
)
@ActivityScope
interface MoviesComponent {

    fun inject(mainFragment: HomeFragment)

    @Component.Builder
    interface Builder {
        fun core(coreComponent: CoreComponent): Builder
        fun recommendationsType(@BindsInstance type: RecommendationsType): Builder
        fun build(): MoviesComponent
    }
}

@Module
interface MoviesModule {
    @Binds
    fun bindMoviesRepository(impl: RealMoviesRepository): MoviesRepository

    @Binds
    fun bindMovieRankingProcessor(impl: RealMovieRankingProcessor): MovieRankingProcessor

    @Binds
    fun bindViewEntitiesMapper(impl: RealMovieViewEntitiesMapper): MovieViewEntitiesMapper
}
