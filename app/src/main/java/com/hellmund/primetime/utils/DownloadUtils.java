package com.hellmund.primetime.utils;

import android.content.Context;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtils {

    private static final String LOG_TAG = "DownloadUtils";
    private static final String API_VERSION = "1";

    public static String downloadURL(String url) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();

        try {
            Log.d(LOG_TAG, "Accessing URL: " + url);
            final Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "";
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error accessing URL: " + url, e);
            return "";
        }
    }

    static String getSimilarMoviesURL(int id) {
        String baseURL = String.format("http://api.themoviedb.org/3/movie/%s/similar", id);
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("sort_by", "popularity.desc")
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .toString();
    }

    static String getSingleMovieDownloadURL(int id) {
        String baseURL = String.format("http://api.themoviedb.org/3/movie/%s/similar", id);
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("sort_by", "popularity.desc")
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .appendQueryParameter("page", Integer.toString(1))
                .toString();
    }

    static String getGenreURL(int genreId) {
        String baseURL = String.format("http://api.themoviedb.org/3/genre/%s/movies", genreId);
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .appendQueryParameter("page", Integer.toString(1))
                .toString();
    }

    static String getTopRatedURL() {
        String baseURL = "http://api.themoviedb.org/3/movie/top_rated";
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .appendQueryParameter("page", Integer.toString(1))
                .toString();
    }

    static String getGenreByYearURL(int year, String genreId) {
        return Uri.parse("http://api.themoviedb.org/3/discover/movie").buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("with_genres", genreId)
                .appendQueryParameter("sort_by", "popularity.desc")
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .appendQueryParameter("primary_release_year", Integer.toString(year))
                .toString();
    }

    static String getVideosURL(int id) {
        String baseURL = String.format("http://api.themoviedb.org/3/movie/%s/videos", id);
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .toString();
    }

    static String getNowPlayingURL(int page) {
        return Uri.parse("http://api.themoviedb.org/3/movie/now_playing").buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .appendQueryParameter("page", Integer.toString(page))
                .toString();
    }

    static String getUpcomingURL(int page) {
        return Uri.parse("http://api.themoviedb.org/3/movie/upcoming").buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .appendQueryParameter("page", Integer.toString(page))
                .toString();
    }

    static String getMovieURL(int id) {
        String baseURL = String.format("http://api.themoviedb.org/3/movie/%s", id);
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .toString();
    }

    public static String getHistoryURL(Context context) {
        return Uri.parse("https://primetime-backend.appspot.com/history").buildUpon()
                .appendQueryParameter("version", API_VERSION)
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .appendQueryParameter("device_id", DeviceUtils.getDeviceID(context))
                .build().toString();
    }

    static String getReleaseDatesURL(int id) {
        String baseURL = String.format("http://api.themoviedb.org/3/movie/%s/release_dates", id);
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .toString();
    }

    public static String getIMDbURL(String imdbId) {
        return "http://www.imdb.com/title/" + imdbId;
    }

    public static String getYouTubeQueryURL(String title) {
        final String query = title + " Trailer";
        return Uri.parse("http://www.youtube.com/results").buildUpon()
                .appendQueryParameter("search_query", query)
                .build().toString();
    }

    static String getMostPopularURL() {
        return Uri.parse("http://api.themoviedb.org/3/movie/popular").buildUpon()
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .build().toString();
    }

    static String getQueryURL(String query) {
        return Uri.parse("http://api.themoviedb.org/3/search/movie").buildUpon()
                .appendQueryParameter("query", query)
                .appendQueryParameter("api_key", DeviceUtils.getApiKey())
                .appendQueryParameter("language", DeviceUtils.getUserLang())
                .build().toString();
    }

    public static String getPosterURL(Context context, String path) {
        return "http://image.tmdb.org/t/p/w500" + path;
    }

    static String getHighResPosterURL(String path) {
        return "http://image.tmdb.org/t/p/w500" + path;
    }

    public static String getLowResPosterURL(String path) {
        return "http://image.tmdb.org/t/p/w500" + path;
    }

}
