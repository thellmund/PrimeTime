package com.hellmund.primetime.ui.history.di

import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.history.RealHistoryRepository
import dagger.Binds
import dagger.Module

@Module
interface HistoryModule {

    @Binds
    fun bindHistoryRepository(impl: RealHistoryRepository): HistoryRepository

}
