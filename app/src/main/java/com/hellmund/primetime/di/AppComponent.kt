package com.hellmund.primetime.di

import android.content.Context
import androidx.work.WorkManager
import com.hellmund.api.di.NetworkModule
import com.hellmund.primetime.App
import com.hellmund.primetime.data.di.DatabaseModule
import com.hellmund.primetime.history.di.HistoryModule
import com.hellmund.primetime.moviedetails.di.MovieDetailsComponent
import com.hellmund.primetime.moviedetails.di.MovieDetailsModule
import com.hellmund.primetime.notifications.NotificationPublisher
import com.hellmund.primetime.onboarding.selectgenres.di.GenresModule
import com.hellmund.primetime.onboarding.selectmovies.di.SelectMoviesModule
import com.hellmund.primetime.recommendations.di.MoviesComponent
import com.hellmund.primetime.recommendations.di.MoviesModule
import com.hellmund.primetime.search.di.SearchModule
import com.hellmund.primetime.search.util.RealStringProvider
import com.hellmund.primetime.search.util.StringProvider
import com.hellmund.primetime.settings.di.SettingsModule
import com.hellmund.primetime.ui.MainActivity
import com.hellmund.primetime.ui_common.util.ImageLoader
import com.hellmund.primetime.ui_common.util.PicassoImageLoader
import com.hellmund.primetime.ui_common.util.RealValueFormatter
import com.hellmund.primetime.ui_common.util.ValueFormatter
import com.hellmund.primetime.watchlist.di.WatchlistModule
import com.hellmund.primetime.workers.GenresPrefetcher
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    DatabaseModule::class,
    NetworkModule::class,
    PersistenceModule::class,
    MoviesModule::class,
    MovieDetailsModule::class,
    SearchModule::class,
    HistoryModule::class,
    GenresModule::class,
    SelectMoviesModule::class,
    SettingsModule::class,
    WatchlistModule::class
])
interface AppComponent {

    fun mainComponent(): MoviesComponent.Factory
    fun movieDetailsComponent(): MovieDetailsComponent.Factory

    fun inject(app: App)
    fun inject(genresWorker: GenresPrefetcher.RefreshGenresWorker)
    fun inject(mainActivity: MainActivity)
    fun inject(notificationPublisher: NotificationPublisher)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

}

@Module
interface AppModule {

    @ContributesAndroidInjector
    fun contributeAndroidInjector(): MainActivity

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
