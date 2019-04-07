package com.hellmund.primetime;

import android.app.Application;

import com.hellmund.primetime.utils.NotificationUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationUtils.createChannel(this);
        NotificationUtils.scheduleNotifications(this);
    }
}
