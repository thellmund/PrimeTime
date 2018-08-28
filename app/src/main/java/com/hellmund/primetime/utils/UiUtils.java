package com.hellmund.primetime.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.hellmund.primetime.R;

public class UiUtils {

    public static void showToast(Context context, int resId) {
        showToast(context, context.getString(resId));
    }

    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, int resId, int length) {
        showToast(context, context.getString(resId), length);
    }

    public static void showToast(Context context, String message, int length) {
        Toast.makeText(context, message, length).show();
    }

    public static int getSnackbarColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorAccent);
    }

}
