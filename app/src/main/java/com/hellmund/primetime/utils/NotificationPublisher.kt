package com.hellmund.primetime.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hellmund.primetime.database.PrimeTimeDatabase
import org.jetbrains.anko.notificationManager

class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!NotificationUtils.areNotificationsEnabled(context)) {
            return
        }

        val database = PrimeTimeDatabase.getInstance(context)
        val releases = database.watchlistDao().releases().blockingGet()

        if (releases.isEmpty()) {
            return
        }

        val notification = NotificationUtils.buildNotification(context, releases)
        context.notificationManager.notify(0, notification)
    }

}
