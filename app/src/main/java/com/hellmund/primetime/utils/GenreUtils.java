package com.hellmund.primetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.ApiGenre;

import java.util.HashSet;
import java.util.Set;

public class GenreUtils {

    public static ApiGenre[] getGenres(Context context) {
        return new ApiGenre[] {
                new ApiGenre(28, context.getString(R.string.action)),
                new ApiGenre(12, context.getString(R.string.adventure)),
                new ApiGenre(16, context.getString(R.string.animation)),
                new ApiGenre(35, context.getString(R.string.comedy)),
                new ApiGenre(80, context.getString(R.string.crime)),
                new ApiGenre(99, context.getString(R.string.documentary)),
                new ApiGenre(18, context.getString(R.string.drama)),
                new ApiGenre(10751, context.getString(R.string.family)),
                new ApiGenre(14, context.getString(R.string.fantasy)),
                new ApiGenre(10769, context.getString(R.string.foreign)),
                new ApiGenre(36, context.getString(R.string.history)),
                new ApiGenre(27, context.getString(R.string.horror)),
                new ApiGenre(10402, context.getString(R.string.music)),
                new ApiGenre(9648, context.getString(R.string.mystery)),
                new ApiGenre(10749, context.getString(R.string.romance)),
                new ApiGenre(878, context.getString(R.string.science_fiction)),
                new ApiGenre(10770, context.getString(R.string.tv_movie)),
                new ApiGenre(53, context.getString(R.string.thriller)),
                new ApiGenre(10752, context.getString(R.string.war)),
                new ApiGenre(37, context.getString(R.string.western))
        };
    }

    public static String[] getGenreNames(Context context) {
        ApiGenre[] genres = getGenres(context);
        String[] names = new String[genres.length];

        for (int i = 0; i < genres.length; i++) {
            names[i] = genres[i].getName();
        }

        return names;
    }

    public static String[] getGenreIds(Context context) {
        ApiGenre[] genres = getGenres(context);
        String[] names = new String[genres.length];

        for (int i = 0; i < genres.length; i++) {
            names[i] = Integer.toString(genres[i].getId());
        }

        return names;
    }

    public static int getGenreID(Context context, String genreName) {
        ApiGenre[] genres = getGenres(context);

        for (ApiGenre genre : genres) {
            if (genre.getName().equals(genreName)) {
                return genre.getId();
            }
        }

        return -1;
    }

    public static String getGenreName(Context context, int genreId) {
        ApiGenre[] genres = getGenres(context);

        for (ApiGenre genre : genres) {
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
