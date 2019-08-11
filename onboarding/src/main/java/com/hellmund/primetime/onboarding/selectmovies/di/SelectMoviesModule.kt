package com.hellmund.primetime.onboarding.selectmovies.di

import com.hellmund.primetime.onboarding.selectmovies.domain.RealSamplesRepository
import com.hellmund.primetime.onboarding.selectmovies.domain.SamplesRepository
import com.hellmund.primetime.onboarding.selectmovies.ui.SelectMoviesFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface SelectMoviesModule {

    @ContributesAndroidInjector
    fun contributeFragmentInjector(): SelectMoviesFragment

    @Binds
    fun bindSamplesRepository(impl: RealSamplesRepository): SamplesRepository

}
