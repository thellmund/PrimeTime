package com.hellmund.primetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.Genre;

import java.util.HashSet;
import java.util.Set;

public class GenreUtils {

    public static Genre[] getGenres(Context context) {
        return new Genre[] {
                new Genre(28, context.getString(R.string.action)),
                new Genre(12, context.getString(R.string.adventure)),
                new Genre(16, context.getString(R.string.animation)),
                new Genre(35, context.getString(R.string.comedy)),
                new Genre(80, context.getString(R.string.crime)),
                new Genre(99, context.getString(R.string.documentary)),
                new Genre(18, context.getString(R.string.drama)),
                new Genre(10751, context.getString(R.string.family)),
                new Genre(14, context.getString(R.string.fantasy)),
                new Genre(10769, context.getString(R.string.foreign)),
                new Genre(36, context.getString(R.string.history)),
                new Genre(27, context.getString(R.string.horror)),
                new Genre(10402, context.getString(R.string.music)),
                new Genre(9648, context.getString(R.string.mystery)),
                new Genre(10749, context.getString(R.string.romance)),
                new Genre(878, context.getString(R.string.science_fiction)),
                new Genre(10770, context.getString(R.string.tv_movie)),
                new Genre(53, context.getString(R.string.thriller)),
                new Genre(10752, context.getString(R.string.war)),
                new Genre(37, context.getString(R.string.western))
        };
    }

    public static String[] getGenreNames(Context context) {
        Genre[] genres = getGenres(context);
        String[] names = new String[genres.length];

        for (int i = 0; i < genres.length; i++) {
            names[i] = genres[i].getName();
        }

        return names;
    }

    public static String[] getGenreIds(Context context) {
        Genre[] genres = getGenres(context);
        String[] names = new String[genres.length];

        for (int i = 0; i < genres.length; i++) {
            names[i] = Integer.toString(genres[i].getId());
        }

        return names;
    }

    public static int getGenreID(Context context, String genreName) {
        Genre[] genres = getGenres(context);

        for (Genre genre : genres) {
            if (genre.getName().equals(genreName)) {
                return genre.getId();
            }
        }

        return -1;
    }

    public static String getGenreName(Context context, int genreId) {
        Genre[] genres = getGenres(context);

        for (Genre genre : genres) {
            if (genre.getId() == genreId) {
                return genre.getName();
            }
        }

        return null;
    }

    public static Set<String> getPreferredGenres(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getStringSet(Constants.KEY_INCLUDED, new HashSet<>());
    }

    public static Set<String> getExcludedGenres(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getStringSet(Constants.KEY_EXCLUDED, new HashSet<>());
    }

}
