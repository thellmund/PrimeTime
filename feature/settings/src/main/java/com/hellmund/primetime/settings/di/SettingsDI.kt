package com.hellmund.primetime.settings.di

import com.hellmund.primetime.settings.ui.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface SettingsModule {

    @ContributesAndroidInjector
    fun contributeFragmentInjector(): SettingsFragment

}