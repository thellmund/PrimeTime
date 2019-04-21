package com.hellmund.primetime

import android.app.Application

import com.hellmund.primetime.utils.NotificationUtils

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createChannel(this)
        NotificationUtils.scheduleNotifications(this)
    }

}
