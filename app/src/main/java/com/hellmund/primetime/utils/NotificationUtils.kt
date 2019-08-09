package com.hellmund.primetime.utils

import android.app.AlarmManager
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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.WatchlistMovie
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
        context.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
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
            .setColor(ContextCompat.getColor(context, R.color.red_400))
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

        val alarmManager = context.getSystemService<AlarmManager>()
        alarmManager?.setRepeating(RTC, alarmTime.toEpochMilli(), INTERVAL_DAY, pendingIntent)
    }

    @JvmStatic
    fun areNotificationsEnabled(context: Context): Boolean {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getBoolean(Preferences.KEY_NOTIFICATIONS, true)
    }

}
