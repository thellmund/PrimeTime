package com.hellmund.primetime.di

import android.content.Context
import com.hellmund.primetime.data.workers.GenresPrefetcher
import com.hellmund.primetime.ui.history.HistoryFragment
import com.hellmund.primetime.ui.history.di.HistoryModule
import com.hellmund.primetime.ui.introduction.IntroductionActivity
import com.hellmund.primetime.ui.search.SearchFragment
import com.hellmund.primetime.ui.selectgenres.SelectGenresActivity
import com.hellmund.primetime.ui.selectgenres.di.GenresModule
import com.hellmund.primetime.ui.selectmovies.di.SelectMoviesComponent
import com.hellmund.primetime.ui.selectstreamingservices.SelectStreamingServicesActivity
import com.hellmund.primetime.ui.selectstreamingservices.di.StreamingServiceModule
import com.hellmund.primetime.ui.settings.SettingsFragment
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.ui.suggestions.di.MovieDetailsComponent
import com.hellmund.primetime.ui.suggestions.di.MoviesComponent
import com.hellmund.primetime.ui.suggestions.di.MoviesModule
import com.hellmund.primetime.ui.watchlist.WatchlistFragment
import com.hellmund.primetime.ui.watchlist.di.WatchlistModule
import com.hellmund.primetime.ui.watchlist.di.WatchlistMovieComponent
import com.hellmund.primetime.utils.*
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    NetworkModule::class,
    PersistenceModule::class,
    MoviesModule::class,
    HistoryModule::class,
    GenresModule::class,
    WatchlistModule::class,
    StreamingServiceModule::class
])
interface AppComponent {

    fun mainComponent(): MoviesComponent.Builder
    fun movieDetailsComponent(): MovieDetailsComponent.Builder
    fun selectMoviesComponent(): SelectMoviesComponent
    fun watchlistMovieComponent(): WatchlistMovieComponent.Builder

    fun inject(genresWorker: GenresPrefetcher.RefreshGenresWorker)
    fun inject(historyFragment: HistoryFragment)
    fun inject(introductionActivity: IntroductionActivity)
    fun inject(notificationPublisher: NotificationPublisher)
    fun inject(mainActivity: MainActivity)
    fun inject(searchFragment: SearchFragment)
    fun inject(selectGenresActivity: SelectGenresActivity)
    fun inject(selectStreamingServicesActivity: SelectStreamingServicesActivity)
    fun inject(settingsFragment: SettingsFragment)
    fun inject(watchlistFragment: WatchlistFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent

    }

}

@Module
interface AppModule {

    @Singleton
    @Binds
    fun bindImageLoader(impl: GlideImageLoader): ImageLoader

    @Singleton
    @Binds
    fun bindValueFormatter(impl: RealValueFormatter): ValueFormatter

    @Singleton
    @Binds
    fun bindStringProvider(impl: RealStringProvider): StringProvider

}
