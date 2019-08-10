package com.hellmund.primetime.di

import android.content.Context
import androidx.work.WorkManager
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.PicassoImageLoader
import com.hellmund.primetime.data.workers.GenresPrefetcher
import com.hellmund.primetime.history.di.HistoryModule
import com.hellmund.primetime.history.ui.HistoryFragment
import com.hellmund.primetime.onboarding.selectgenres.di.GenresModule
import com.hellmund.primetime.onboarding.selectgenres.ui.SelectGenresFragment
import com.hellmund.primetime.onboarding.selectmovies.di.SelectMoviesComponent
import com.hellmund.primetime.search.di.SearchModule
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.search.util.RealStringProvider
import com.hellmund.primetime.search.util.StringProvider
import com.hellmund.primetime.settings.ui.SettingsFragment
import com.hellmund.primetime.ui.MainActivity
import com.hellmund.primetime.ui.introduction.IntroductionActivity
import com.hellmund.primetime.ui.selectstreamingservices.SelectStreamingServicesActivity
import com.hellmund.primetime.ui.selectstreamingservices.di.StreamingServiceModule
import com.hellmund.primetime.moviedetails.di.MovieDetailsComponent
import com.hellmund.primetime.moviedetails.di.MovieDetailsModule
import com.hellmund.primetime.ui.recommendations.di.MoviesComponent
import com.hellmund.primetime.ui.recommendations.di.MoviesModule
import com.hellmund.primetime.utils.NotificationPublisher
import com.hellmund.primetime.watchlist.di.WatchlistModule
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import dagger.Binds
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
    MoviesModule::class,
    MovieDetailsModule::class,
    SearchModule::class,
    HistoryModule::class,
    GenresModule::class,
    WatchlistModule::class,
    StreamingServiceModule::class
])
interface AppComponent {

    fun mainComponent(): MoviesComponent.Factory
    fun movieDetailsComponent(): MovieDetailsComponent.Factory
    fun selectMoviesComponent(): SelectMoviesComponent

    fun inject(genresWorker: GenresPrefetcher.RefreshGenresWorker)
    fun inject(historyFragment: HistoryFragment)
    fun inject(introductionActivity: IntroductionActivity)
    fun inject(notificationPublisher: NotificationPublisher)
    fun inject(mainActivity: MainActivity)
    fun inject(searchFragment: SearchFragment)
    fun inject(selectGenresFragment: SelectGenresFragment)
    fun inject(selectStreamingServicesActivity: SelectStreamingServicesActivity)
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
    fun bindValueFormatter(impl: com.hellmund.primetime.ui_common.RealValueFormatter): com.hellmund.primetime.ui_common.ValueFormatter

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
