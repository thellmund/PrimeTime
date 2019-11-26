package com.hellmund.primetime.moviedetails.di

import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.moviedetails.data.MovieDetailsRepository
import com.hellmund.primetime.moviedetails.data.RealMovieDetailsRepository
import com.hellmund.primetime.moviedetails.ui.MovieDetailsFragment
import com.hellmund.primetime.ui_common.MovieViewEntity
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    modules = [MovieDetailsModule::class],
    dependencies = [CoreComponent::class]
)
@ActivityScope
interface MovieDetailsComponent {

    fun inject(movieDetailsFragment: MovieDetailsFragment)

    @Component.Builder
    interface Builder {
        fun core(coreComponent: CoreComponent): Builder
        fun movie(@BindsInstance movie: MovieViewEntity): Builder
        fun build(): MovieDetailsComponent
    }
}

@Module
interface MovieDetailsModule {
    @Binds
    fun bindVideosRepository(impl: RealMovieDetailsRepository): MovieDetailsRepository
}
