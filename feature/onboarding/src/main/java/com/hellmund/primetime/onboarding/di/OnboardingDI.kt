package com.hellmund.primetime.onboarding.di

import androidx.fragment.app.FragmentActivity
import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.onboarding.OnboardingActivity
import com.hellmund.primetime.onboarding.domain.RealSamplesRepository
import com.hellmund.primetime.onboarding.domain.SamplesRepository
import com.hellmund.primetime.onboarding.ui.OnboardingNavigator
import com.hellmund.primetime.onboarding.ui.RealOnboardingNavigator
import com.hellmund.primetime.onboarding.ui.selectgenres.SelectGenresFragment
import com.hellmund.primetime.onboarding.ui.selectmovies.SelectMoviesFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    modules = [OnboardingModule::class],
    dependencies = [CoreComponent::class]
)
@ActivityScope
interface OnboardingComponent {
    fun inject(onboardingActivity: OnboardingActivity)
    fun inject(selectGenresFragment: SelectGenresFragment)
    fun inject(selectMoviesFragment: SelectMoviesFragment)

    @Component.Builder
    interface Builder {

        fun core(coreComponent: CoreComponent): Builder

        @BindsInstance
        fun activity(activity: FragmentActivity): Builder

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

    @Binds
    fun bindOnboardingNavigator(impl: RealOnboardingNavigator): OnboardingNavigator
}
