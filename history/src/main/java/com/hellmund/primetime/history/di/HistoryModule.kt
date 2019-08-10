package com.hellmund.primetime.history.di

import com.hellmund.primetime.data.HistoryRepository
import com.hellmund.primetime.data.RealHistoryRepository
import dagger.Binds
import dagger.Module

@Module
interface HistoryModule {

    @Binds
    fun bindHistoryRepository(impl: RealHistoryRepository): HistoryRepository

}
