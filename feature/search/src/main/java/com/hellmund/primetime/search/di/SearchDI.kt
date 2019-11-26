package com.hellmund.primetime.search.di

import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.search.data.RealSearchRepository
import com.hellmund.primetime.search.data.SearchRepository
import com.hellmund.primetime.search.ui.SearchFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    modules = [SearchModule::class],
    dependencies = [CoreComponent::class]
)
@ActivityScope
interface SearchComponent {
    fun inject(searchFragment: SearchFragment)

    @Component.Builder
    interface Builder {
        fun core(coreComponent: CoreComponent): Builder
        fun build(): SearchComponent
    }
}

@Module
interface SearchModule {
    @Binds
    fun bindSearchRepository(impl: RealSearchRepository): SearchRepository
}
