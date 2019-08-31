package com.hellmund.primetime.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object PersistenceModule {

    /*@JvmStatic
    @Singleton
    @Provides
    fun provideDatabase(
        context: Context
    ): AppDatabase {
        return Room
            .databaseBuilder(context, AppDatabase::class.java, "db")
            .fallbackToDestructiveMigration()
            .build()
    }*/

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefs(
        context: Context
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

}
