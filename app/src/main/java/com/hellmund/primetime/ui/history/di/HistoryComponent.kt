package com.hellmund.primetime.ui.history.di

import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.ui.history.HistoryRepository
import dagger.Module
import dagger.Provides

@Module
class HistoryModule {

    @Provides
    fun provideHistoryRepository(
            database: AppDatabase
    ): HistoryRepository = HistoryRepository(database)

}
