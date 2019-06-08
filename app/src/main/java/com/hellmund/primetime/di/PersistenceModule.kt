package com.hellmund.primetime.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.hellmund.primetime.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Singleton

@Module
object PersistenceModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideDatabase(
            context: Context
    ): AppDatabase {
        return Room
                .databaseBuilder(context, AppDatabase::class.java, "db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefs(
            context: Context
    ): SharedPreferences = context.defaultSharedPreferences

}
