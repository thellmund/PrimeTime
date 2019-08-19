package com.hellmund.primetime.history.di

import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.data.repositories.RealHistoryRepository
import com.hellmund.primetime.history.ui.HistoryFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface HistoryModule {

    @ContributesAndroidInjector
    fun contributeFragmentInjector(): HistoryFragment

    @Binds
    fun bindHistoryRepository(impl: RealHistoryRepository): HistoryRepository

}
