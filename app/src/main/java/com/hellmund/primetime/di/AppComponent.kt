package com.hellmund.primetime.di

import android.content.Context
import androidx.work.WorkManager
import com.hellmund.primetime.App
import com.hellmund.primetime.ui_common.util.ImageLoader
import com.hellmund.primetime.ui_common.util.PicassoImageLoader
import com.hellmund.primetime.history.di.HistoryModule
import com.hellmund.primetime.history.ui.HistoryFragment
import com.hellmund.primetime.moviedetails.di.MovieDetailsComponent
import com.hellmund.primetime.moviedetails.di.MovieDetailsModule
import com.hellmund.primetime.notifications.NotificationPublisher
import com.hellmund.primetime.onboarding.selectgenres.di.GenresModule
import com.hellmund.primetime.onboarding.selectmovies.di.SelectMoviesModule
import com.hellmund.primetime.recommendations.di.MoviesComponent
import com.hellmund.primetime.recommendations.di.MoviesModule
import com.hellmund.primetime.search.di.SearchModule
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.search.util.RealStringProvider
import com.hellmund.primetime.search.util.StringProvider
import com.hellmund.primetime.settings.ui.SettingsFragment
import com.hellmund.primetime.ui.MainActivity
import com.hellmund.primetime.ui_common.util.RealValueFormatter
import com.hellmund.primetime.ui_common.util.ValueFormatter
import com.hellmund.primetime.watchlist.di.WatchlistModule
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import com.hellmund.primetime.workers.GenresPrefetcher
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    NetworkModule::class,
    PersistenceModule::class,
    MoviesModule::class,
    MovieDetailsModule::class,
    SearchModule::class,
    HistoryModule::class,
    GenresModule::class,
    SelectMoviesModule::class,
    WatchlistModule::class
])
interface AppComponent {

    fun mainComponent(): MoviesComponent.Factory
    fun movieDetailsComponent(): MovieDetailsComponent.Factory

    fun inject(app: App)
    fun inject(genresWorker: GenresPrefetcher.RefreshGenresWorker)
    fun inject(historyFragment: HistoryFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(notificationPublisher: NotificationPublisher)
    fun inject(searchFragment: SearchFragment)
    fun inject(settingsFragment: SettingsFragment)
    fun inject(watchlistFragment: WatchlistFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

}

@Module
interface AppModule {

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

    }

}
