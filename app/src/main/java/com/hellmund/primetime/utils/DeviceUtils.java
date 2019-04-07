package com.hellmund.primetime.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.hellmund.primetime.BuildConfig;

import java.util.Locale;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class DeviceUtils {

    public static String getApiKey() {
        return BuildConfig.API_KEY;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    static boolean isSlowConnection(Context context) {
        final TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final int networkType = telephonyManager.getNetworkType();

        return networkType == TelephonyManager.NETWORK_TYPE_GPRS
                || networkType == TelephonyManager.NETWORK_TYPE_EDGE;
    }

    static boolean isLowRam(Context context) {
        final ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && manager.isLowRamDevice();
    }

    public static boolean isConnected(Context context) {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static String getUserLang() {
        String lang = Locale.getDefault().getLanguage();
        final String DEFAULT_LANG = "en";

        if (lang.equals("at") || lang.equals("ch")) {
            lang = "de";
        } else if (!lang.equals("de")) {
            lang = DEFAULT_LANG;
        }

        return lang;
    }

    public static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE;
    }

}
