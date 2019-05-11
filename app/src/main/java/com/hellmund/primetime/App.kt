package com.hellmund.primetime

import android.app.Application
import com.hellmund.primetime.di.AppComponent
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.utils.NotificationUtils.createChannel
import com.hellmund.primetime.utils.NotificationUtils.scheduleNotifications
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        buildComponent()
        initThreeTen()
        initTimber()

        createChannel(this)
        scheduleNotifications(this)
    }

    private fun buildComponent() {
        appComponent = DaggerAppComponent
                .builder()
                .context(this)
                .build()
    }

    private fun initThreeTen() {
        AndroidThreeTen.init(this)
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

}
