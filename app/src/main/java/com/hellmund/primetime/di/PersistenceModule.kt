package com.hellmund.primetime.di

import android.arch.persistence.room.Room
import android.content.Context
import android.content.SharedPreferences
import com.hellmund.primetime.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Singleton

@Module
class PersistenceModule {

    @Singleton
    @Provides
    fun provideDatabase(
            context: Context
    ): AppDatabase {
        return Room
                .databaseBuilder(context, AppDatabase::class.java, "db")
                .allowMainThreadQueries() // TODO
                .build()
    }

    @Singleton
    @Provides
    fun provideSharedPrefs(
            context: Context
    ): SharedPreferences = context.defaultSharedPreferences

}
