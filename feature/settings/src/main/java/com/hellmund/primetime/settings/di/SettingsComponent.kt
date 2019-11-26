package com.hellmund.primetime.settings.di

import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.settings.ui.SettingsFragment
import dagger.Component

@Component(
    dependencies = [CoreComponent::class]
)
@ActivityScope
interface SettingsComponent {
    fun inject(settingsFragment: SettingsFragment)

    @Component.Builder
    interface Builder {
        fun core(coreComponent: CoreComponent): Builder
        fun build(): SettingsComponent
    }
}
