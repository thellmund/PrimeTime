package com.hellmund.primetime.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    static Date getDateFromIsoString(String isoStr) {
        String dateStr = isoStr.split("T")[0];
        String[] dates = dateStr.split("-");

        Calendar result = getMidnightCalendar();
        result.set(Calendar.YEAR, Integer.parseInt(dates[0]));
        result.set(Calendar.MONTH, Integer.parseInt(dates[1]) - 1);
        result.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[2]));

        return result.getTime();
    }

    public static Calendar getMidnightCalendar() {
        Calendar result = Calendar.getInstance();
        result.set(Calendar.HOUR, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }

    @SuppressLint("SimpleDateFormat")
    public static Date getDateFromString(String str) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static long getEpochFromString(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").parse(dateStr).getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    public static String getDateInLocalFormat(Calendar cal) {
        return getDateInLocalFormat(cal.getTimeInMillis());
    }

    public static String getDateInLocalFormat(Date date) {
        return getDateInLocalFormat(date.getTime());
    }

    public static String getDateInLocalFormat(long timestamp) {
        final Locale locale = Locale.getDefault();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        if (isGerman(locale)) {
            return String.format("%s.%s.%s", cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR));
        } else {
            return String.format("%s/%s/%s", cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.YEAR));
        }
    }

    private static boolean isGerman(Locale locale) {
        final String countryCode = locale.getCountry();
        return countryCode.equals(Locale.GERMANY.getCountry())
                || countryCode.equals(Locale.GERMAN.getCountry());
    }

    public static String formatRuntime(int runtime) {
        final String hours = String.format(Locale.getDefault(), "%01d", runtime / 60);
        final String minutes = String.format(Locale.getDefault(), "%02d", runtime % 60);
        return String.format("%s:%s", hours, minutes);
    }

}
