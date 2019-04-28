package com.hellmund.primetime

import android.app.Application
import com.hellmund.primetime.di.AppComponent
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.utils.NotificationUtils

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        buildComponent()

        NotificationUtils.createChannel(this)
        NotificationUtils.scheduleNotifications(this)
    }

    private fun buildComponent() {
        appComponent = DaggerAppComponent
                .builder()
                .context(this)
                .build()
    }

}
