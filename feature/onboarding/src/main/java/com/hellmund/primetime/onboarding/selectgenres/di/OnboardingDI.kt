package com.hellmund.primetime.onboarding.selectgenres.di

import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.onboarding.selectgenres.ui.SelectGenresFragment
import com.hellmund.primetime.onboarding.selectmovies.domain.RealSamplesRepository
import com.hellmund.primetime.onboarding.selectmovies.domain.SamplesRepository
import com.hellmund.primetime.onboarding.selectmovies.ui.SelectMoviesFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    modules = [OnboardingModule::class],
    dependencies = [CoreComponent::class]
)
@ActivityScope
interface OnboardingComponent {
    fun inject(selectGenresFragment: SelectGenresFragment)
    fun inject(selectMoviesFragment: SelectMoviesFragment)

    @Component.Builder
    interface Builder {
        fun core(coreComponent: CoreComponent): Builder
        fun build(): OnboardingComponent
    }
}

interface OnboardingComponentProvider {
    fun provideOnboardingComponent(): OnboardingComponent
}

@Module
interface OnboardingModule {

    @Binds
    fun bindSamplesRepository(impl: RealSamplesRepository): SamplesRepository

    // TODO Use correct Scope for repository

}
