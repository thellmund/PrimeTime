package com.hellmund.primetime.onboarding.selectmovies.di

import com.hellmund.primetime.onboarding.selectmovies.domain.RealSamplesRepository
import com.hellmund.primetime.onboarding.selectmovies.domain.SamplesRepository
import com.hellmund.primetime.onboarding.selectmovies.ui.SelectMoviesFragment
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
