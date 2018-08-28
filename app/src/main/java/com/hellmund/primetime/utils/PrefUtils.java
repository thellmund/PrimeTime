package com.hellmund.primetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

    public static void setHasDownloadedHistoryInRealm(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putBoolean("hasDownloadedHistory", true).apply();
    }

    public static boolean hasDownloadedHistoryInRealm(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("hasDownloadedHistory", false);
    }

}
