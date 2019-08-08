package com.hellmund.primetime

import android.app.Application
import com.hellmund.primetime.data.workers.GenresPrefetcher
import com.hellmund.primetime.di.AppComponent
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.utils.NotificationUtils.createChannel
import com.hellmund.primetime.utils.NotificationUtils.scheduleNotifications
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import javax.inject.Inject

class App : Application() {

    @Inject
    lateinit var genresPrefetcher: GenresPrefetcher

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        initThreeTen()
        initTimber()

        createChannel(this)
        scheduleNotifications(this)

        refreshGenres()
    }

    private fun initThreeTen() {
        AndroidThreeTen.init(this)
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun refreshGenres() {
        genresPrefetcher.run()
    }

}
