package com.hellmund.primetime.ui.selectmovies.di

import com.hellmund.primetime.ui.onboarding.SelectMoviesFragment
import com.hellmund.primetime.ui.selectmovies.RealSamplesRepository
import com.hellmund.primetime.ui.selectmovies.SamplesRepository
import dagger.Binds
import dagger.Module
import dagger.Subcomponent

@Subcomponent(modules = [SelectMoviesModule::class])
interface SelectMoviesComponent {
    fun inject(selectMoviesFragment: SelectMoviesFragment)
}

@Module
interface SelectMoviesModule {

    @Binds
    fun bindSamplesRepository(impl: RealSamplesRepository): SamplesRepository

}
