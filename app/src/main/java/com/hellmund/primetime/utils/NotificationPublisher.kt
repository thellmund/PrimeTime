package com.hellmund.primetime.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hellmund.primetime.di.app
import com.hellmund.primetime.ui.watchlist.WatchlistRepository
import org.jetbrains.anko.notificationManager
import javax.inject.Inject

class NotificationPublisher : BroadcastReceiver() {

    @Inject
    lateinit var watchlistRepository: WatchlistRepository

    @SuppressLint("CheckResult")
    override fun onReceive(context: Context, intent: Intent) {
        context.app.appComponent.inject(this)

        if (!NotificationUtils.areNotificationsEnabled(context)) {
            return
        }

        watchlistRepository
                .getReleases()
                .filter { it.isNotEmpty() }
                .map { NotificationUtils.buildNotification(context, it) }
                .subscribe { context.notificationManager.notify(0, it) }
    }

}
