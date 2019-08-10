package com.hellmund.primetime.search.di

import com.hellmund.primetime.search.data.RealSearchRepository
import com.hellmund.primetime.search.data.SearchRepository
import dagger.Binds
import dagger.Module

@Module
interface SearchModule {

    @Binds
    fun bindSearchRepository(impl: RealSearchRepository): SearchRepository

}