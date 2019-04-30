package com.hellmund.primetime.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hellmund.primetime.di.app
import com.hellmund.primetime.watchlist.WatchlistRepository
import org.jetbrains.anko.notificationManager
import javax.inject.Inject

class NotificationPublisher : BroadcastReceiver() {

    @Inject
    lateinit var watchlistRepository: WatchlistRepository

    override fun onReceive(context: Context, intent: Intent) {
        context.app.appComponent.inject(this)

        if (!NotificationUtils.areNotificationsEnabled(context)) {
            return
        }

        val releases = watchlistRepository.getReleases().blockingGet()
        if (releases.isEmpty()) {
            return
        }

        val notification = NotificationUtils.buildNotification(context, releases)
        context.notificationManager.notify(0, notification)
    }

}
