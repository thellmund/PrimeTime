package com.hellmund.primetime.onboarding.selectgenres.di

import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.repositories.RealGenresRepository
import com.hellmund.primetime.onboarding.selectgenres.ui.SelectGenresFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface GenresModule {

    @ContributesAndroidInjector
    fun contributeFragmentInjector(): SelectGenresFragment

    @Binds
    fun bindGenresRepository(impl: RealGenresRepository): GenresRepository

}
