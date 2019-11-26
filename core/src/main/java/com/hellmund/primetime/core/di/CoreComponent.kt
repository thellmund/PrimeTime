package com.hellmund.primetime.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.hellmund.api.TmdbApiService
import com.hellmund.api.di.NetworkModule
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.PicassoImageLoader
import com.hellmund.primetime.core.RealStringProvider
import com.hellmund.primetime.core.RealValueFormatter
import com.hellmund.primetime.core.StringProvider
import com.hellmund.primetime.core.ValueFormatter
import com.hellmund.primetime.data.di.DatabaseModule
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.data.repositories.WatchlistRepository
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Component(
    modules = [
        CoreModule::class,
        DatabaseModule::class,
        NetworkModule::class
    ]
)
@Singleton
interface CoreComponent {
    fun context(): Context
    fun genresRepository(): GenresRepository
    fun historyRepository(): HistoryRepository
    fun imageLoader(): ImageLoader
    fun sharedPrefs(): SharedPreferences
    fun stringProvider(): StringProvider
    fun tmdbApiService(): TmdbApiService
    fun valueFormatter(): ValueFormatter
    fun watchlistRepository(): WatchlistRepository
    fun workManager(): WorkManager

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): CoreComponent
    }
}

@Module
interface CoreModule {

    @Singleton
    @Binds
    fun bindImageLoader(impl: PicassoImageLoader): ImageLoader

    @Singleton
    @Binds
    fun bindValueFormatter(impl: RealValueFormatter): ValueFormatter

    @Singleton
    @Binds
    fun bindStringProvider(impl: RealStringProvider): StringProvider

    @Module
    companion object {

        @Singleton
        @JvmStatic
        @Provides
        fun provideWorkManager(
            context: Context
        ) = WorkManager.getInstance(context)

        @JvmStatic
        @Singleton
        @Provides
        fun provideSharedPrefs(
            context: Context
        ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }
}
