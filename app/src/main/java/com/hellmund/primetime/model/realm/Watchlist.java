package com.hellmund.primetime.model.realm;

import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.model.SearchResult;
import com.hellmund.primetime.model.WatchlistMovie;
import com.hellmund.primetime.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class Watchlist {

    public static ArrayList<WatchlistMovie> get() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RealmWatchlistMovie> watchlist = realm
                .where(RealmWatchlistMovie.class)
                .sort("timestamp")
                .findAll();

        final int length = watchlist.size();
        ArrayList<WatchlistMovie> results = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            results.add(new WatchlistMovie(watchlist.get(i)));
        }

        realm.close();
        return results;
    }

    public static ArrayList<WatchlistMovie> getReleasesToday() {
        Date today = DateUtils.getMidnightCalendar().getTime();

        Realm realm = Realm.getDefaultInstance();
        RealmResults<RealmWatchlistMovie> results =
                realm.where(RealmWatchlistMovie.class)
                        .equalTo("releaseDate", today)
                        .equalTo("notificationsActivated", true)
                        .findAll();

        ArrayList<WatchlistMovie> releasesToday = new ArrayList<>();
        final int length = results.size();

        for (int i = 0; i < length; i++) {
            releasesToday.add(new WatchlistMovie(results.get(i)));
        }

        realm.close();
        return releasesToday;
    }

    public static void add(Movie movie) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            RealmWatchlistMovie sameMovie = getMovieById(movie.getID());

            if (sameMovie != null) {
                return;
            }

            RealmWatchlistMovie watchlistMovie = r.createObject(RealmWatchlistMovie.class);
            watchlistMovie.setId(movie.getID());
            watchlistMovie.setTitle(movie.getTitle());
            watchlistMovie.setPosterUrl(movie.getPosterURL());
            watchlistMovie.setReleaseDate(movie.getReleaseDate());
            watchlistMovie.setTimestamp(System.currentTimeMillis());
            watchlistMovie.setNotificationsActivated(true);

            if (movie.getRuntime() != null) {
                watchlistMovie.setRuntime(movie.getRuntime());
            }
        });
    }

    public static void add(SearchResult result) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            RealmWatchlistMovie watchlistMovie = r.createObject(RealmWatchlistMovie.class);
            watchlistMovie.setId(result.getID());
            watchlistMovie.setPosterUrl(result.getPosterPath());
            watchlistMovie.setTitle(result.getTitle());
            watchlistMovie.setRuntime(result.getRuntime());
            watchlistMovie.setReleaseDate(result.getReleaseDate());
            watchlistMovie.setTimestamp(System.currentTimeMillis());
            watchlistMovie.setNotificationsActivated(true);
            r.copyToRealm(watchlistMovie);
        });
    }

    public static void update(WatchlistMovie movie) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            RealmWatchlistMovie realmMovie = getMovieById(movie.getID());
            if (realmMovie != null) {
                realmMovie.setRuntime(movie.getRuntime());
            }
        });
    }

    public static boolean contains(int id) {
        Realm realm = Realm.getDefaultInstance();
        final boolean onWatchlist =
                realm.where(RealmWatchlistMovie.class).equalTo("id", id).findFirst() != null;
        realm.close();
        return onWatchlist;
    }

    public static void remove(int id) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            RealmWatchlistMovie result = getMovieById(id);
            if (result != null) {
                result.deleteFromRealm();
            }
        });
    }

    private static RealmWatchlistMovie getMovieById(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(RealmWatchlistMovie.class).equalTo("id", id).findFirst();
    }

    public static void restore(WatchlistMovie watchlistMovie) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            RealmWatchlistMovie movie = r.createObject(RealmWatchlistMovie.class);
            movie.setId(watchlistMovie.getID());
            movie.setTitle(watchlistMovie.getTitle());
            movie.setPosterUrl(watchlistMovie.getPosterURL());
            movie.setRuntime(watchlistMovie.getRuntime());
            movie.setTimestamp(watchlistMovie.getTimestamp());
        });
    }

}
