package com.hellmund.primetime.di

import android.content.Context
import com.hellmund.primetime.ui.history.HistoryFragment
import com.hellmund.primetime.ui.introduction.IntroductionActivity
import com.hellmund.primetime.ui.introduction.SplashScreenActivity
import com.hellmund.primetime.ui.search.SearchFragment
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.selectgenres.SelectGenresActivity
import com.hellmund.primetime.ui.selectmovies.SelectMoviesActivity
import com.hellmund.primetime.ui.settings.SettingsFragment
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.ui.suggestions.MainFragment
import com.hellmund.primetime.ui.suggestions.di.SuggestionComponent
import com.hellmund.primetime.ui.watchlist.WatchlistFragment
import com.hellmund.primetime.ui.watchlist.di.WatchlistModule
import com.hellmund.primetime.ui.watchlist.di.WatchlistMovieComponent
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.NotificationPublisher
import com.hellmund.primetime.utils.RealValueFormatter
import com.hellmund.primetime.utils.ValueFormatter
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

    fun suggestionComponent(): SuggestionComponent.Builder
    fun watchlistMovieComponent(): WatchlistMovieComponent.Builder

    fun inject(historyFragment: HistoryFragment)
    fun inject(introductionActivity: IntroductionActivity)
    fun inject(notificationPublisher: NotificationPublisher)
    fun inject(mainActivity: MainActivity)
    fun inject(mainFragment: MainFragment)
    fun inject(searchFragment: SearchFragment)
    fun inject(selectGenresActivity: SelectGenresActivity)
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
    fun provideImageLoader(
            context: Context
    ): ImageLoader = ImageLoader.with(context)

    @Singleton
    @Provides
    fun provideValueFormatter(
            context: Context,
            genresRepository: GenresRepository
    ): ValueFormatter = RealValueFormatter(context, genresRepository)

}
