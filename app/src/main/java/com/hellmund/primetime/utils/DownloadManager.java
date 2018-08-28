package com.hellmund.primetime.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.hellmund.primetime.model.HistoryMovie;
import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.model.MovieFactory;
import com.hellmund.primetime.model.Sample;
import com.hellmund.primetime.model.SearchResult;
import com.hellmund.primetime.model.realm.History;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class DownloadManager {

    public static ArrayList<Movie> downloadPersonalRecommendations(Context context) {
        ArrayList<HistoryMovie> history = History.getLatestLiked();
        ArrayList<Movie> results = new ArrayList<>();
        final int length = Math.min(history.size(), 10);

        for (int i = 0; i < length; i++) {
            final int referrerId = history.get(i).getID();
            final String url = DownloadUtils.getSimilarMoviesURL(history.get(i).getID());
            final String resultStr = DownloadUtils.downloadURL(url);
            ArrayList<Movie> similarMovies = getMovies(resultStr, Constants.MOVIE_REFERRER, referrerId);
            results.addAll(similarMovies);
        }

        final Set<String> genreIds = GenreUtils.getPreferredGenres(context);
        try {
            for (String genreId : genreIds) {
                final int id = Integer.parseInt(genreId);
                results.addAll(downloadGenreRecommendations(context, id));
            }
        } catch (NullPointerException e) {
            Log.i("DownloadManager", "Error loading genre recommendations", e);
        }

        results.addAll(downloadTopRatedMovies(context));

        return new ResultBuilder(context, results)
                .removeDuplicates()
                .removeUnreleasedAndKnown()
                .removeExcludedGenres()
                .sort()
                .build();
    }

    private static ArrayList<Movie> getMovies(String str, int referrerType, int referrerId) {
        ArrayList<Movie> data = new ArrayList<>();

        try {
            JSONArray results = new JSONObject(str).getJSONArray("results");

            final int length = results.length();
            for (int i = 0; i < length; i++) {
                try {
                    JSONObject obj = results.getJSONObject(i);
                    data.add(MovieFactory.from(obj, referrerType, referrerId));
                } catch (JSONException e) {
                    Log.d("DownloadMgr", "Error when loading movie no. " + i, e);
                }
            }
        } catch (JSONException e) {
            Log.e("DownloadMgr", "Error downloading recommendations:\n" + str, e);
            data = new ArrayList<>();
        }

        return data;
    }

    public static ArrayList<Movie> downloadSingleMovieRecommendations(Context context, int id) {
        final String url = DownloadUtils.getSingleMovieDownloadURL(id);
        final String resultStr = DownloadUtils.downloadURL(url);
        ArrayList<Movie> similarMovies = getMovies(resultStr, Constants.MOVIE_REFERRER, id);

        return new ResultBuilder(context, similarMovies)
                .removeDuplicates()
                .removeUnreleasedAndKnown()
                .removeExcludedGenres()
                .sort()
                .build();
    }

    public static ArrayList<Movie> downloadGenreRecommendations(Context context, int genreId) {
        final String url = DownloadUtils.getGenreURL(genreId);
        final String resultStr = DownloadUtils.downloadURL(url);
        ArrayList<Movie> movies = getMovies(resultStr, Constants.GENRE_REFERRER, genreId);

        return new ResultBuilder(context, movies)
                .removeDuplicates()
                .removeUnreleasedAndKnown()
                .sort()
                .build();
    }

    private static ArrayList<Movie> downloadTopRatedMovies(Context context) {
        final String url = DownloadUtils.getTopRatedURL();
        final String resultStr = DownloadUtils.downloadURL(url);
        ArrayList<Movie> movies = getMovies(resultStr, Constants.GENRE_REFERRER, 0);

        return new ResultBuilder(context, movies)
                .removeDuplicates()
                .removeUnreleasedAndKnown()
                .sort()
                .build();
    }

    public static ArrayList<Sample> downloadSamples(Context context) {
        Set<String> genres = GenreUtils.getPreferredGenres(context);
        final int moviesPerGenre = 30 / genres.size();

        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final int startYear = currentYear - 4;

        ArrayList<Sample> results = new ArrayList<>();

        for (String genre : genres) {
            ArrayList<Sample> genreResults = new ArrayList<>();

            for (int year = startYear; year <= currentYear; year++) {
                String url = DownloadUtils.getGenreByYearURL(year, genre);
                String resultStr = DownloadUtils.downloadURL(url);
                ArrayList<Sample> yearResults = getSamples(resultStr);
                genreResults.addAll(yearResults);
            }

            Collections.sort(genreResults);

            Date today = DateUtils.getMidnightCalendar().getTime();

            for (int i = 0; i < genreResults.size(); i++) {
                Sample sample = genreResults.get(i);
                if (results.contains(sample) || sample.getReleaseDate().after(today)) {
                    genreResults.remove(i--);
                }
            }

            results.addAll(genreResults.subList(0, moviesPerGenre));
        }

        return results;
    }

    private static ArrayList<Sample> getSamples(String resultStr) {
        try {
            JSONArray results = new JSONObject(resultStr).getJSONArray("results");
            final int length = results.length();

            ArrayList<Sample> samples = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                Sample sample = new Sample(results.getJSONObject(i));
                samples.add(sample);
            }

            return samples;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    public static String downloadTrailerUrl(int id) {
        final String url = DownloadUtils.getVideosURL(id);
        final String resultStr = DownloadUtils.downloadURL(url);

        try {
            JSONArray results = new JSONObject(resultStr).getJSONArray("results");
            int i = 0;

            while (i < results.length()) {
                if (results.getJSONObject(i).getString("site").equals("YouTube")) {
                    final String key = results.getJSONObject(i).getString("key");
                    return "https://www.youtube.com/watch?v=" + key;
                }
            }

            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    public static Movie downloadMovie(int id) {
        String url = DownloadUtils.getMovieURL(id);
        String resultStr = DownloadUtils.downloadURL(url);

        try {
            return new Movie(new JSONObject(resultStr));
        } catch (JSONException e) {
            return null;
        }
    }

    public static ArrayList<Movie> downloadNowPlayingMovies(Context context) {
        ArrayList<Movie> results = new ArrayList<>();
        int page = 1;

        while (results.size() < 25) {
            String url = DownloadUtils.getNowPlayingURL(page);
            String resultStr = DownloadUtils.downloadURL(url);
            results.addAll(getMovies(resultStr, Constants.NOW_PLAYING_REFERRER, Constants.NOW_PLAYING_RECOMMENDATION));
            page++;
        }

        return new ResultBuilder(context, results)
                .removeDuplicates()
                .removeKnown()
                .removeExcludedGenres()
                .build();
    }

    public static ArrayList<Movie> downloadUpcomingMovies(Context context) {
        ArrayList<Movie> results = new ArrayList<>();
        int page = 1;

        while (results.size() < 25) {
            String url = DownloadUtils.getUpcomingURL(page);
            String resultStr = DownloadUtils.downloadURL(url);

            ArrayList<Movie> pageResults = getMovies(resultStr, Constants.NOW_PLAYING_REFERRER, Constants.UPCOMING_RECOMMENDATION);
            final int length = pageResults.size();

            for (int i = 0; i < length; i++) {
                final int id = pageResults.get(i).getID();
                final Date countryReleaseDate = downloadCountryReleaseDate(context, id);

                if (countryReleaseDate != null) {
                    pageResults.get(i).setReleaseDate(countryReleaseDate);
                }
            }

            results.addAll(pageResults);
            page++;
        }

        return new ResultBuilder(context, results)
                .removeDuplicates()
                .removeKnown()
                .removeExcludedGenres()
                .build();
    }

    public static int downloadRuntime(int id) {
        Movie movie = downloadMovie(id);
        if (movie != null) {
            return movie.getRuntime();
        } else {
            return 0;
        }
    }

    public static String downloadTitle(int id) {
        Movie movie = downloadMovie(id);
        if (movie != null) {
            return movie.getTitle();
        } else {
            return null;
        }
    }

    public static Date downloadCountryReleaseDate(Context context, int id) {
        final int THEATRICAL_RELEASE = 3;

        try {
            final String url = DownloadUtils.getReleaseDatesURL(id);
            final String responseStr = DownloadUtils.downloadURL(url);
            JSONArray countries = new JSONObject(responseStr).getJSONArray("results");

            String countryCode;

            TelephonyManager telephonyMgr =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyMgr != null) {
                countryCode = telephonyMgr.getSimCountryIso();
            } else {
                countryCode = Locale.getDefault().getCountry();
            }

            for (int i = 0; i < countries.length(); i++) {
                String isoStr = countries.getJSONObject(i).getString("iso_3166_1");
                if (isoStr.equalsIgnoreCase(countryCode)) {
                    JSONArray releases = countries.getJSONObject(i).getJSONArray("release_dates");

                    for (int j = 0; j < releases.length(); j++) {
                        final int type = releases.getJSONObject(j).getInt("type");
                        if (type == THEATRICAL_RELEASE) {
                            String releaseDate = releases.getJSONObject(j).getString("release_date");
                            return DateUtils.getDateFromIsoString(releaseDate);
                        }
                    }
                }
            }

            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    public static ArrayList<String> downloadMostPopularMoviePosters() {
        ArrayList<String> posterUrls;

        try {
            String resultStr = DownloadUtils.downloadURL(DownloadUtils.getMostPopularURL());
            JSONArray results = new JSONObject(resultStr).getJSONArray("results");
            final int length = results.length();
            posterUrls = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                String path = results.getJSONObject(i).getString("poster_path");
                posterUrls.add(DownloadUtils.getHighResPosterURL(path));
            }
        } catch (JSONException e) {
            posterUrls = new ArrayList<>();
        }

        return posterUrls;
    }

    public static ArrayList<SearchResult> downloadSearchResults(String query) {
        ArrayList<SearchResult> results = new ArrayList<>();

        try {
            String responseStr = DownloadUtils.downloadURL(DownloadUtils.getQueryURL(query));
            JSONArray searchResults = new JSONObject(responseStr).getJSONArray("results");
            final int length = searchResults.length();

            for (int i = 0; i < length; i++) {
                results.add(SearchResult.fromJSON(searchResults.getJSONObject(i)));
            }
        } catch (JSONException e) {
            results = new ArrayList<>();
        }

        return results;
    }

}
