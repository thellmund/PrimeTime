package com.hellmund.primetime.onboarding.selectgenres.di

import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.repositories.RealGenresRepository
import dagger.Binds
import dagger.Module

@Module
interface GenresModule {

    @Binds
    fun bindGenresRepository(impl: RealGenresRepository): GenresRepository

}
