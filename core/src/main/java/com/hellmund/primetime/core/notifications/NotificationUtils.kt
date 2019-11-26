package com.hellmund.primetime.core.notifications

import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_DAY
import android.app.AlarmManager.RTC
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.hellmund.primetime.core.Preferences
import com.hellmund.primetime.core.R
import com.hellmund.primetime.data.model.WatchlistMovie
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import javax.inject.Inject

private const val GROUP_KEY = "releases_group"
private const val CHANNEL_ID = "releases"

class NotificationUtils @Inject constructor(
    private val context: Context
) {

    private fun buildNotification(movie: WatchlistMovie): Notification {
        val header = context.getString(R.string.new_release_notif_header)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(header)
            .setContentText(movie.title)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.red_400))
            .setGroup(GROUP_KEY)
            .build()
    }

    fun scheduleNotification(watchlistMovie: WatchlistMovie) {
        val notificationId = watchlistMovie.id.toInt()
        val notification = buildNotification(watchlistMovie)
        val date = watchlistMovie.releaseDate
        scheduleNotification(notificationId, notification, date)
    }

    private fun scheduleNotification(
        notificationId: Int,
        notification: Notification,
        date: LocalDate
    ) {
        val notificationIntent = NotificationReceiver.newIntent(context, notificationId, notification)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0)

        val alarmTime = date.atTime(9, 0, 0, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(RTC, alarmTime.toEpochMilli(), INTERVAL_DAY, pendingIntent)
    }

    fun cancelNotification(watchlistMovie: WatchlistMovie) {
        val notificationId = watchlistMovie.id.toInt()
        val notification = buildNotification(watchlistMovie)
        val notificationIntent = NotificationReceiver.newIntent(context, notificationId, notification)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0)

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        @JvmStatic
        fun areNotificationsEnabled(context: Context): Boolean {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPrefs.getBoolean(Preferences.KEY_NOTIFICATIONS, true)
        }

        @JvmStatic
        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return
            }

            val name = context.getString(R.string.release_notifications)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
