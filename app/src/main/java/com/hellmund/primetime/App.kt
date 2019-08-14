package com.hellmund.primetime

import android.app.Application
import com.hellmund.primetime.di.AppComponent
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.moviedetails.ui.MovieDetailsFragment
import com.hellmund.primetime.notifications.NotificationUtils.createChannel
import com.hellmund.primetime.notifications.NotificationUtils.scheduleNotifications
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.workers.GenresPrefetcher
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class App : Application(), HasAndroidInjector,
    MovieDetailsFragment.ComponentProvider, HomeFragment.ComponentProvider {

    lateinit var appComponent: AppComponent

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var genresPrefetcher: GenresPrefetcher

    override fun onCreate() {
        super.onCreate()
        initThreeTen()
        initTimber()

        appComponent = DaggerAppComponent.factory().create(this)
        appComponent.inject(this)

        createChannel(this)
        scheduleNotifications(this)
        prefetchGenres()
    }

    private fun initThreeTen() {
        AndroidThreeTen.init(this)
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun prefetchGenres() {
        genresPrefetcher.run()
    }

    override fun moviesComponent() = appComponent.mainComponent()

    override fun movieDetailsComponent() = appComponent.movieDetailsComponent()

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

}
