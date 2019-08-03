package com.hellmund.primetime

import android.app.Application
import com.hellmund.primetime.di.AppComponent
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.utils.NotificationUtils.createChannel
import com.hellmund.primetime.utils.NotificationUtils.scheduleNotifications
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

class App : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        initThreeTen()
        initTimber()

        createChannel(this)
        scheduleNotifications(this)
    }

    private fun initThreeTen() {
        AndroidThreeTen.init(this)
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

}
