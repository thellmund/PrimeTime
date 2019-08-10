package com.hellmund.primetime.onboarding.selectgenres.di

import com.hellmund.primetime.data.GenresRepository
import com.hellmund.primetime.data.RealGenresRepository
import dagger.Binds
import dagger.Module

@Module
interface GenresModule {

    @Binds
    fun bindGenresRepository(impl: RealGenresRepository): GenresRepository

}
