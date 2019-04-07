package com.hellmund.primetime;

import android.app.Application;

import com.hellmund.primetime.utils.NotificationUtils;

import io.realm.Realm;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        NotificationUtils.createChannel(this);
        NotificationUtils.scheduleNotifications(this);
    }
}
