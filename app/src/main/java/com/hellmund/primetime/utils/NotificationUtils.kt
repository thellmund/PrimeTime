package com.hellmund.primetime.utils

import android.app.AlarmManager.INTERVAL_DAY
import android.app.AlarmManager.RTC
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.hellmund.primetime.R
import com.hellmund.primetime.database.WatchlistMovie
import org.jetbrains.anko.alarmManager
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.notificationManager
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

object NotificationUtils {
    
    private const val GROUP_KEY = "releases_group"
    private const val CHANNEL_ID = "releases"

    @JvmStatic
    fun createChannel(context: Context) {
        if (SDK_INT < O) {
            return
        }

        val name = context.getString(R.string.release_notifications)
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
        context.notificationManager.createNotificationChannel(channel)
    }

    @JvmStatic
    fun buildNotification(context: Context, movies: List<WatchlistMovie>): Notification {
        val inboxStyle = NotificationCompat.InboxStyle()
        val titles = movies.map { it.title }
        titles.forEach { title -> inboxStyle.addLine(title) }

        val header = context.resources.getQuantityString(
                R.plurals.new_releases_notif_header, movies.size, movies.size)
        inboxStyle.setBigContentTitle(header)

        val collapsedText = titles.joinToString(", ")

        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(inboxStyle)
                .setContentTitle(header)
                .setContentText(collapsedText)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.app_color))
                .setGroup(GROUP_KEY)
                .build()
    }

    @JvmStatic
    fun scheduleNotifications(context: Context) {
        val notificationIntent = Intent(context, NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0)

        val alarmTime = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .withHour(9)
                .toInstant()

        context.alarmManager.setRepeating(RTC, alarmTime.toEpochMilli(), INTERVAL_DAY, pendingIntent)
    }

    @JvmStatic
    fun areNotificationsEnabled(context: Context): Boolean {
        return context.defaultSharedPreferences.getBoolean(Constants.KEY_NOTIFICATIONS, true)
    }

}
