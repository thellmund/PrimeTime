package com.hellmund.primetime.history.di

import com.hellmund.primetime.database.AppDatabase
import com.hellmund.primetime.history.HistoryRepository
import dagger.Module
import dagger.Provides

/*@Subcomponent(modules = [HistoryModule::class])
interface HistoryComponent {
    fun inject(historyFragment: HistoryFragment)
}*/

@Module
class HistoryModule {

    @Provides
    fun provideHistoryRepository(
            database: AppDatabase
    ): HistoryRepository = HistoryRepository(database)

}
