package com.hellmund.primetime.search.di

import com.hellmund.primetime.search.data.RealSearchRepository
import com.hellmund.primetime.search.data.SearchRepository
import com.hellmund.primetime.search.ui.SearchFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface SearchModule {

    @ContributesAndroidInjector
    fun contributeAndroidInjector(): SearchFragment

    @Binds
    fun bindSearchRepository(impl: RealSearchRepository): SearchRepository

}
