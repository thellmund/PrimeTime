package com.hellmund.primetime.history.di

import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.data.repositories.RealHistoryRepository
import dagger.Binds
import dagger.Module

@Module
interface HistoryModule {

    @Binds
    fun bindHistoryRepository(impl: RealHistoryRepository): HistoryRepository

}