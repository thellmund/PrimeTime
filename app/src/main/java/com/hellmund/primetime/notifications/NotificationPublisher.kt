package com.hellmund.primetime.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.hellmund.primetime.di.app
import com.hellmund.primetime.data.repositories.WatchlistRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

class NotificationPublisher : BroadcastReceiver() {

    @Inject
    lateinit var watchlistRepository: WatchlistRepository

    override fun onReceive(context: Context, intent: Intent) {
        context.app.appComponent.inject(this)

        if (!NotificationUtils.areNotificationsEnabled(context)) {
            return
        }

        GlobalScope.launch {
            val releases = watchlistRepository.getReleases(LocalDate.now())
            if (releases.isEmpty()) {
                return@launch
            }

            val notification = NotificationUtils.buildNotification(context, releases)
            val notificationManager = context.getSystemService<NotificationManager>()
            notificationManager?.notify(0, notification)
        }
    }

}