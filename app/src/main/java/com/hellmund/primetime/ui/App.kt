package com.hellmund.primetime.ui

import android.app.Application
import com.hellmund.primetime.core.BuildConfig
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.core.di.CoreComponentProvider
import com.hellmund.primetime.core.di.DaggerCoreComponent
import com.hellmund.primetime.core.notifications.NotificationUtils
import com.jakewharton.threetenabp.AndroidThreeTen

class App : Application(), CoreComponentProvider {

    override val coreComponent: CoreComponent by lazy {
        DaggerCoreComponent.builder()
            .context(this)
            .apiKey(BuildConfig.TMDB_API_KEY)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        NotificationUtils.createChannel(this)
    }
}
