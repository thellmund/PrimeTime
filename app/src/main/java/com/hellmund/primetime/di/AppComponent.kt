package com.hellmund.primetime.di

import android.content.Context
import android.content.SharedPreferences
import com.hellmund.primetime.history.HistoryFragment
import com.hellmund.primetime.introduction.IntroductionActivity
import com.hellmund.primetime.main.MainFragment
import com.hellmund.primetime.search.SearchFragment
import com.hellmund.primetime.selectmovies.SelectMoviesActivity
import com.hellmund.primetime.settings.SettingsFragment
import com.hellmund.primetime.splash.SplashScreenActivity
import com.hellmund.primetime.utils.GenresProvider
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.RealGenresProvider
import com.hellmund.primetime.watchlist.WatchlistFragment
import com.hellmund.primetime.watchlist.di.WatchlistModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    NetworkModule::class,
    PersistenceModule::class,
    WatchlistModule::class
])
interface AppComponent {

    fun inject(historyFragment: HistoryFragment)
    fun inject(introductionActivity: IntroductionActivity)
    fun inject(mainFragment: MainFragment)
    fun inject(searchFragment: SearchFragment)
    fun inject(selectMoviesActivity: SelectMoviesActivity)
    fun inject(settingsFragment: SettingsFragment)
    fun inject(splashScreenActivity: SplashScreenActivity)
    fun inject(watchlistFragment: WatchlistFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent

    }

}

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideGenresProvider(
            sharedPrefs: SharedPreferences
    ): GenresProvider = RealGenresProvider(sharedPrefs)

    @Singleton
    @Provides
    fun provideImageLoader(
            context: Context
    ): ImageLoader = ImageLoader.with(context)

}
