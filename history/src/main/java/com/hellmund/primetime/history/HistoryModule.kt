package com.hellmund.primetime.history

import dagger.Binds
import dagger.Module

@Module
interface HistoryModule {

    @Binds
    fun bindHistoryRepository(impl: RealHistoryRepository): HistoryRepository

}
