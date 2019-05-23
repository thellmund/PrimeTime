package com.hellmund.primetime.ui.selectgenres.di

import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.selectgenres.RealGenresRepository
import dagger.Binds
import dagger.Module

@Module
interface GenresModule {

    @Binds
    fun bindGenresRepository(impl: RealGenresRepository): GenresRepository

}
