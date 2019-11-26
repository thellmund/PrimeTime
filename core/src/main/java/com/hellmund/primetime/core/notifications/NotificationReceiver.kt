package com.hellmund.primetime.core.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent

private const val NOTIFICATION_ID_EXTRA = "NOTIFICATION_ID_EXTRA"
private const val NOTIFICATION_EXTRA = "NOTIFICATION_EXTRA"

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!NotificationUtils.areNotificationsEnabled(context)) {
            // The user has disabled notifications
            return
        }

        val notificationId = intent.getIntExtra(NOTIFICATION_ID_EXTRA, 0)
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION_EXTRA)

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        fun newIntent(
            context: Context,
            notificationId: Int,
            notification: Notification
        ) = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NOTIFICATION_ID_EXTRA, notificationId)
            putExtra(NOTIFICATION_EXTRA, notification)
        }
    }
}
