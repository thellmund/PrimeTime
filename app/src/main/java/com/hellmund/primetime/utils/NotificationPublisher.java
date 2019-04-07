package com.hellmund.primetime.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hellmund.primetime.model.WatchlistMovie;

import java.util.ArrayList;
import java.util.Calendar;

public class NotificationPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isCorrectTime() && NotificationUtils.areNotificationsEnabled(context)) {
            ArrayList<WatchlistMovie> releases = new ArrayList<>(); // Watchlist.getReleasesToday();
            if (releases.isEmpty()) {
                return;
            }

            NotificationManager notificationMgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = NotificationUtils.buildNotification(context, releases);
            notificationMgr.notify(0, notification);
        }
    }

    private boolean isCorrectTime() {
        Calendar now = Calendar.getInstance();
        final int hour = now.get(Calendar.HOUR_OF_DAY);
        return hour == 9;
    }

}
