package com.hellmund.primetime.utils;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.hellmund.primetime.R;
import com.hellmund.primetime.database.WatchlistMovie;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationUtils {

    private static final String CHANNEL_ID = "channel";
    private static final String GROUP_KEY = "group";

    @TargetApi(Build.VERSION_CODES.O)
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                "releases", "Release notifications", NotificationManager.IMPORTANCE_LOW);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    static Notification buildNotification(Context context, List<WatchlistMovie> movies) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        for (int i = 0; i < movies.size(); i++) {
            inboxStyle.addLine(movies.get(i).getTitle());
        }

        String header = context.getResources().getQuantityString(
                R.plurals.new_releases_notif_header, movies.size(), movies.size());
        inboxStyle.setBigContentTitle(header);

        ArrayList<String> titles = new ArrayList<>();
        for (WatchlistMovie movie : movies) {
            titles.add(movie.getTitle());
        }

        StringBuilder collapsedText = new StringBuilder();
        if (!titles.isEmpty()) {
            collapsedText.append(titles.get(0));
        }

        int size = titles.size();
        for (int i = 1; i < size; i++) {
            collapsedText.append(", ").append(titles.get(i));
        }

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(inboxStyle)
                .setContentTitle(header)
                .setContentText(collapsedText.toString())
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.app_color))
                .setGroup(GROUP_KEY)
                .build();
    }

    public static void scheduleNotifications(final Context context) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar cal = DateUtils.startOfDay();
        cal.set(Calendar.HOUR_OF_DAY, 9);

        if (alarmMgr != null) {
            alarmMgr.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
                                  AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public static boolean areNotificationsEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.KEY_NOTIFICATIONS, true);
    }

}
