package com.hellmund.primetime.utils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.Locale;

public class DateUtils {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    public static Instant startOfDay() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }

    public static Instant endOfDay() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999)
                .toInstant();
    }

    public static String getDateInLocalFormat(LocalDate date) {
        return formatter.format(date);
    }

    public static String formatRuntime(int runtime) {
        final String hours = String.format(Locale.getDefault(), "%01d", runtime / 60);
        final String minutes = String.format(Locale.getDefault(), "%02d", runtime % 60);
        return String.format("%s:%s", hours, minutes);
    }

}
